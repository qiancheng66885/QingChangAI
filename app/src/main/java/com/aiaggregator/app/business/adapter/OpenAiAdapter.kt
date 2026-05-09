package com.aiaggregator.app.business.adapter

import com.aiaggregator.app.data.model.ApiConfig
import com.aiaggregator.app.data.remote.HttpClientFactory
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import okhttp3.Response
import com.aiaggregator.app.base.utils.LogUtil
import java.io.IOException

/**
 * OpenAI Chat Completions API 适配器。
 * API 文档: https://platform.openai.com/docs/api-reference/chat
 * 兼容所有 OpenAI 格式的平台（DeepSeek、豆包、千问、中转站等）。
 */
class OpenAiAdapter : AiAdapter {

    override val formatType = "openai-compatible"

    private val sseClient: OkHttpClient = HttpClientFactory.createForSse()
    private val restClient: OkHttpClient = HttpClientFactory.create()
    private val json = Json { ignoreUnknownKeys = true }

    override fun streamChat(request: ChatRequest, config: ApiConfig): Flow<ChatChunk> = callbackFlow {
        try {
            val body = buildJsonObject {
            put("model", request.model)
            put("stream", true)
            put("temperature", request.temperature)
            put("max_tokens", request.maxTokens)
            if (request.extraParams["json_mode"] == "true") {
                putJsonObject("response_format") { put("type", "json_object") }
            }
            putJsonObject("stream_options") { put("include_usage", true) }
            putJsonArray("messages") {
                request.messages.forEach { msg -> add(buildOpenAiMessage(msg)) }
            }
        }

        val httpRequest = Request.Builder()
            .url("${config.baseUrl.trimEnd('/').removeSuffix("/v1")}/v1/chat/completions")
            .header("Authorization", "Bearer ${config.apiKey}")
            .header("Content-Type", "application/json")
            .post(body.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val factory = EventSources.createFactory(sseClient)
        val eventSource = factory.newEventSource(httpRequest, object : EventSourceListener() {
            override fun onEvent(es: EventSource, id: String?, type: String?, data: String) {
                if (data == "[DONE]") return
                try {
                    val obj = json.parseToJsonElement(data).jsonObject
                    val choices = obj["choices"]?.jsonArray
                    val delta = choices?.firstOrNull()?.jsonObject?.get("delta")?.jsonObject
                    // Parse text content — do NOT return early, finish_reason is below
                    val contentEl = delta?.get("content")
                    if (contentEl != null && contentEl !is kotlinx.serialization.json.JsonNull) {
                        val text = contentEl.jsonPrimitive.content
                        if (text.isNotBlank()) trySend(ChatChunk(content = text))
                    }
                    val finishReason = choices?.firstOrNull()?.jsonObject
                        ?.get("finish_reason")?.jsonPrimitive?.content
                    if (!finishReason.isNullOrBlank() && finishReason != "null") {
                        trySend(ChatChunk(content = null, isDone = true))
                    }
                } catch (e: Exception) {
                    LogUtil.w("OpenAiSSE", "SSE event parse error, data: ${data.take(100)}", e)
                }
            }

            override fun onClosed(es: EventSource) {
                trySend(ChatChunk(content = null, isDone = true))
                close()
            }

            override fun onFailure(es: EventSource, t: Throwable?, response: okhttp3.Response?) {
                val errMsg = when {
                    response?.code == 401 -> "认证失败，请检查 API 密钥"
                    response?.code == 404 -> "模型不存在，请检查模型名称"
                    response?.code == 429 -> "请求过于频繁，请稍后重试"
                    response != null -> "API 错误 (${response.code})"
                    else -> t?.message ?: "连接失败"
                }
                trySend(ChatChunk(content = null, isDone = true, error = errMsg))
                close(t)
            }
        })

        awaitClose { eventSource.cancel() }
        } catch (e: Exception) {
            trySend(ChatChunk(content = null, isDone = true, error = e.message ?: "连接失败"))
            close(e)
        }
    }

    override suspend fun syncChat(request: ChatRequest, config: ApiConfig): ChatResponse {
        val body = buildJsonObject {
            put("model", request.model)
            put("stream", false)
            put("temperature", request.temperature)
            put("max_tokens", request.maxTokens)
            if (request.extraParams["json_mode"] == "true") {
                putJsonObject("response_format") { put("type", "json_object") }
            }
            putJsonArray("messages") {
                request.messages.forEach { msg -> add(buildOpenAiMessage(msg)) }
            }
        }

        val httpRequest = Request.Builder()
            .url("${config.baseUrl.trimEnd('/').removeSuffix("/v1")}/v1/chat/completions")
            .header("Authorization", "Bearer ${config.apiKey}")
            .header("Content-Type", "application/json")
            .post(body.toString().toRequestBody("application/json".toMediaType()))
            .build()

        return try {
            val response = restClient.newCall(httpRequest).execute()
            val respBody = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                return ChatResponse("", error = "HTTP ${response.code}: ${errorBodySummary(respBody)}")
            }
            validateJsonContentType(response)
            parseResponse(respBody)
        } catch (e: IOException) {
            val url = httpRequest.url.toString()
            ChatResponse("", error = "网络错误: ${e.message}。请求 URL: $url")
        }
    }

    private fun buildOpenAiMessage(msg: ChatMessageItem) = buildJsonObject {
        put("role", msg.role)
        if (msg.imageBase64 != null && msg.role == "user") {
            putJsonArray("content") {
                if (msg.content.isNotBlank()) {
                    add(buildJsonObject {
                        put("type", "text")
                        put("text", msg.content)
                    })
                }
                add(buildJsonObject {
                    put("type", "image_url")
                    putJsonObject("image_url") {
                        put("url", "data:${msg.imageMimeType ?: "image/jpeg"};base64,${msg.imageBase64}")
                    }
                })
            }
        } else {
            put("content", msg.content)
        }
    }

    private fun parseResponse(body: String): ChatResponse = try {
        val obj = json.parseToJsonElement(body).jsonObject
        val content = obj["choices"]?.jsonArray
            ?.firstOrNull()?.jsonObject
            ?.get("message")?.jsonObject
            ?.get("content")?.jsonPrimitive?.content ?: ""
        val usage = obj["usage"]?.jsonObject?.let {
            TokenUsage(
                promptTokens = it["prompt_tokens"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0,
                completionTokens = it["completion_tokens"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0,
                totalTokens = it["total_tokens"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0
            )
        }
        ChatResponse(content = content, usage = usage)
    } catch (e: Exception) {
        ChatResponse("", error = "解析失败: ${e.message}")
    }

    private fun validateJsonContentType(response: Response) {
        val ct = response.body?.contentType()
        if (ct != null && (ct.type != "application" || ct.subtype != "json")) {
            val url = response.request.url.toString()
            val preview = response.peekBody(500).string()
            throw IOException("服务器返回了非 JSON 内容 (${ct.type}/${ct.subtype})，请检查 API 地址是否正确。请求 URL: $url，响应预览: $preview")
        }
    }

    private fun errorBodySummary(body: String): String {
        return if (body.length > 200) body.take(200) + "..." else body
    }
}
