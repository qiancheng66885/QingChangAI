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
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import okhttp3.Response
import com.aiaggregator.app.base.utils.LogUtil
import java.io.IOException

class AnthropicAdapter : AiAdapter {

    override val formatType = "anthropic-compatible"

    private val sseClient = HttpClientFactory.sseClient
    private val restClient = HttpClientFactory.client
    private val json = Json { ignoreUnknownKeys = true }

    override fun streamChat(request: ChatRequest, config: ApiConfig): Flow<ChatChunk> = callbackFlow {
        var inputTokens = 0
        try {
            val body = buildJsonObject {
                put("model", request.model)
                put("stream", true)
                put("max_tokens", request.maxTokens)
                put("temperature", request.temperature)

                val systemMsg = request.messages.find { it.role == "system" }
                if (systemMsg != null) {
                    put("system", systemMsg.content)
                }

                putJsonArray("messages") {
                    request.messages.filter { it.role != "system" }.forEach { msg ->
                        add(buildAnthropicMessage(msg))
                    }
                }
            }

            val httpRequest = Request.Builder()
                .url("${config.baseUrl.trimEnd('/')}/v1/messages")
                .header("x-api-key", config.apiKey)
                .header("anthropic-version", "2023-06-01")
                .header("Content-Type", "application/json")
                .post(body.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val factory = EventSources.createFactory(sseClient)
            val eventSource = factory.newEventSource(httpRequest, object : EventSourceListener() {
                override fun onEvent(es: EventSource, id: String?, type: String?, data: String) {
                    try {
                        val obj = json.parseToJsonElement(data).jsonObject
                        val eventType = obj["type"]?.jsonPrimitive?.content ?: return

                        when (eventType) {
                            "message_start" -> {
                                obj["message"]?.jsonObject
                                    ?.get("usage")?.jsonObject
                                    ?.get("input_tokens")?.jsonPrimitive?.content
                                    ?.toIntOrNull()?.let { inputTokens = it }
                            }
                            "content_block_delta" -> {
                                val delta = obj["delta"]?.jsonObject
                                val textEl = delta?.get("text") ?: return
                                if (textEl !is kotlinx.serialization.json.JsonNull) {
                                    val text = textEl.jsonPrimitive.content
                                    if (text.isNotBlank()) trySend(ChatChunk(content = text))
                                }
                            }
                            "message_delta" -> {
                                val delta = obj["delta"]?.jsonObject
                                val stopReason = delta?.get("stop_reason")?.jsonPrimitive?.content
                                if (stopReason != null) {
                                    val outputTokens = obj["usage"]?.jsonObject
                                        ?.get("output_tokens")?.jsonPrimitive?.content?.toIntOrNull() ?: 0
                                    trySend(ChatChunk(content = null, isDone = true,
                                        usage = TokenUsage(inputTokens, outputTokens, inputTokens + outputTokens)))
                                    close()
                                }
                            }
                            "message_stop" -> {
                                close()
                            }
                            "error" -> {
                                val errorMsg = obj["error"]?.jsonObject
                                    ?.get("message")?.jsonPrimitive?.content ?: "未知错误"
                                trySend(ChatChunk(content = null, isDone = true, error = errorMsg))
                                close()
                            }
                        }
                    } catch (e: Exception) {
                        LogUtil.w("AnthropicSSE", "SSE parse error: ${data.take(100)}", e)
                        trySend(ChatChunk(content = null, isDone = true, error = "SSE 解析错误: ${e.message}"))
                        close()
                    }
                }

                override fun onClosed(es: EventSource) {
                    trySend(ChatChunk(content = null, isDone = true))
                    close()
                }

                override fun onFailure(es: EventSource, t: Throwable?, response: Response?) {
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
            put("max_tokens", request.maxTokens)
            put("temperature", request.temperature)

            val systemMsg = request.messages.find { it.role == "system" }
            if (systemMsg != null) put("system", systemMsg.content)

            putJsonArray("messages") {
                request.messages.filter { it.role != "system" }.forEach { msg ->
                    add(buildAnthropicMessage(msg))
                }
            }
        }

        val httpRequest = Request.Builder()
            .url("${config.baseUrl.trimEnd('/')}/v1/messages")
            .header("x-api-key", config.apiKey)
            .header("anthropic-version", "2023-06-01")
            .header("Content-Type", "application/json")
            .post(body.toString().toRequestBody("application/json".toMediaType()))
            .build()

        return try {
            val response = restClient.newCall(httpRequest).execute()
            val respBody = response.body?.string() ?: ""
            if (!response.isSuccessful) return ChatResponse("", error = "HTTP ${response.code}: ${AdapterUtils.errorBodySummary(respBody)}")
            AdapterUtils.validateJsonContentType(response)
            parseResponse(respBody)
        } catch (e: IOException) {
            ChatResponse("", error = e.message ?: "网络连接失败")
        }
    }

    private fun buildAnthropicMessage(msg: ChatMessageItem) = buildJsonObject {
        put("role", msg.role)
        putJsonArray("content") {
            if (msg.content.isNotBlank()) {
                add(buildJsonObject { put("type", "text"); put("text", msg.content) })
            }
            if (msg.imageBase64 != null) {
                add(buildJsonObject {
                    put("type", "image")
                    putJsonObject("source") {
                        put("type", "base64")
                        put("media_type", msg.imageMimeType ?: "image/jpeg")
                        put("data", msg.imageBase64!!)
                    }
                })
            }
        }
    }

    private fun parseResponse(body: String): ChatResponse = try {
        val obj = json.parseToJsonElement(body).jsonObject
        val content = obj["content"]?.jsonArray
            ?.firstOrNull()?.jsonObject
            ?.get("text")?.jsonPrimitive?.content ?: ""
        val usage = obj["usage"]?.jsonObject?.let {
            val prompt = it["input_tokens"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0
            val completion = it["output_tokens"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0
            TokenUsage(promptTokens = prompt, completionTokens = completion, totalTokens = prompt + completion)
        }
        ChatResponse(content = content, usage = usage)
    } catch (e: Exception) {
        ChatResponse("", error = "解析失败: ${e.message}")
    }
}
