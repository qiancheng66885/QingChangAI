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
import com.aiaggregator.app.base.utils.LogUtil
import java.io.IOException

class OpenAiAdapter : AiAdapter {

    override val formatType = "openai-compatible"

    private val sseClient = HttpClientFactory.sseClient
    private val restClient = HttpClientFactory.client
    private val json = Json { ignoreUnknownKeys = true }

    override fun streamChat(request: ChatRequest, config: ApiConfig): Flow<ChatChunk> = callbackFlow {
        var doneSent = false
        try {
            val body = buildJsonObject {
                put("model", request.model)
                put("stream", true)
                put("temperature", request.temperature)
                put("max_tokens", request.maxTokens)
                if (request.extraParams["json_mode"] == "true") {
                    putJsonObject("response_format") { put("type", "json_object") }
                }
                putJsonArray("messages") {
                    request.messages.forEach { msg -> add(buildOpenAiMessage(msg)) }
                }
            }

            val (endpoint, isCustomEp) = AdapterUtils.effectiveChatEndpoint(config)
            val (authName, authValue) = AdapterUtils.buildAuthHeader(config.apiKey, config.formatType, config.authHeaderName)
            val httpRequest = Request.Builder()
                .url(AdapterUtils.buildUrl(config.baseUrl, endpoint, isCustomEp))
                .header(authName, authValue)
                .header("Content-Type", "application/json")
                .post(body.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val factory = EventSources.createFactory(sseClient)
            val eventSource = factory.newEventSource(httpRequest, object : EventSourceListener() {
                override fun onEvent(es: EventSource, id: String?, type: String?, data: String) {
                    if (data == "[DONE]") {
                        if (!doneSent) { doneSent = true; trySend(ChatChunk(content = null, isDone = true)) }
                        return
                    }
                    try {
                        val obj = json.parseToJsonElement(data).jsonObject
                        val choices = obj["choices"]?.jsonArray
                        val delta = choices?.firstOrNull()?.jsonObject?.get("delta")?.jsonObject
                        val contentEl = delta?.get("content")
                        if (contentEl != null && contentEl !is kotlinx.serialization.json.JsonNull) {
                            val text = contentEl.jsonPrimitive.content
                            if (text.isNotBlank()) trySend(ChatChunk(content = text))
                        }
                        val finishReason = choices?.firstOrNull()?.jsonObject
                            ?.get("finish_reason")?.jsonPrimitive?.content
                        if (!finishReason.isNullOrBlank() && finishReason != "null") {
                            if (!doneSent) { doneSent = true; trySend(ChatChunk(content = null, isDone = true)) }
                        }
                    } catch (e: Exception) {
                        LogUtil.w("OpenAiSSE", "SSE parse error: ${data.take(100)}", e)
                        if (!doneSent) {
                            doneSent = true
                            trySend(ChatChunk(content = null, isDone = true, error = "SSE 解析错误: ${e.message}"))
                        }
                        close()
                    }
                }

                override fun onClosed(es: EventSource) {
                    if (!doneSent) { doneSent = true; trySend(ChatChunk(content = null, isDone = true)) }
                    close()
                }

                override fun onFailure(es: EventSource, t: Throwable?, response: okhttp3.Response?) {
                    val errMsg = when {
                        response != null -> AdapterUtils.errorDetail(response)
                        else -> t?.message ?: "连接失败"
                    }
                    if (!doneSent) { doneSent = true; trySend(ChatChunk(content = null, isDone = true, error = errMsg)) }
                    close(t)
                }
            })

            awaitClose { eventSource.cancel() }
        } catch (e: Exception) {
            if (!doneSent) { doneSent = true; trySend(ChatChunk(content = null, isDone = true, error = e.message ?: "连接失败")) }
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

        val (endpoint, isCustomEp) = AdapterUtils.effectiveChatEndpoint(config)
        val (authName, authValue) = AdapterUtils.buildAuthHeader(config.apiKey, config.formatType, config.authHeaderName)
        val httpRequest = Request.Builder()
            .url(AdapterUtils.buildUrl(config.baseUrl, endpoint, isCustomEp))
            .header(authName, authValue)
            .header("Content-Type", "application/json")
            .post(body.toString().toRequestBody("application/json".toMediaType()))
            .build()

        return try {
            restClient.newCall(httpRequest).execute().use { response ->
                if (!response.isSuccessful) {
                    val respBody = response.body?.string() ?: ""
                    return ChatResponse("", error = AdapterUtils.errorDetail(response, respBody))
                }
                AdapterUtils.validateJsonContentType(response)
                val respBody = response.body?.string() ?: ""
                parseResponse(respBody)
            }
        } catch (e: IOException) {
            ChatResponse("", error = e.message ?: "网络连接失败")
        }
    }

    private fun buildOpenAiMessage(msg: ChatMessageItem) = buildJsonObject {
        put("role", msg.role)
        if (msg.images.isNotEmpty() && msg.role == "user") {
            putJsonArray("content") {
                if (msg.content.isNotBlank()) {
                    add(buildJsonObject {
                        put("type", "text")
                        put("text", msg.content)
                    })
                }
                msg.images.forEach { img ->
                    add(buildJsonObject {
                        put("type", "image_url")
                        putJsonObject("image_url") {
                            put("url", "data:${img.mimeType};base64,${img.base64}")
                        }
                    })
                }
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
}
