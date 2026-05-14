package com.aiaggregator.app.business.adapter

import com.aiaggregator.app.data.model.ApiConfig
import com.aiaggregator.app.data.remote.HttpClientFactory
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
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
                if (request.extraParams["deep_think"] == "true") {
                    if (shouldUseReasoningEffort(request.model, config.baseUrl)) {
                        put("reasoning_effort", request.extraParams["reasoning_effort"] ?: "high")
                    }
                    if (shouldUseReasoningObject(request.model, config.baseUrl)) {
                        putJsonObject("reasoning") {
                            put("effort", request.extraParams["reasoning_effort"] ?: "high")
                            put("exclude", false)
                        }
                    }
                    if (shouldUseThinkingObject(request.model, config.baseUrl)) {
                        putJsonObject("thinking") {
                            put("type", "enabled")
                        }
                    }
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
                        val choice = choices?.firstOrNull()?.jsonObject
                        val delta = choice?.get("delta") as? JsonObject
                        val reasoning = firstReasoning(delta, "reasoning_content", "reasoning", "thinking", "reasoning_details")
                            ?: firstReasoning(obj, "reasoning_content", "reasoning", "thinking", "reasoning_details")
                        if (!reasoning.isNullOrBlank()) {
                            trySend(ChatChunk(content = null, reasoningContent = reasoning))
                        }
                        val text = contentText(delta?.get("content"))
                            ?: contentText(delta?.get("text"))
                        if (!text.isNullOrBlank()) {
                            trySend(ChatChunk(content = text))
                        }
                        val finishReason = choice?.get("finish_reason")?.jsonPrimitive?.content
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
            if (request.extraParams["deep_think"] == "true") {
                if (shouldUseReasoningEffort(request.model, config.baseUrl)) {
                    put("reasoning_effort", request.extraParams["reasoning_effort"] ?: "high")
                }
                if (shouldUseReasoningObject(request.model, config.baseUrl)) {
                    putJsonObject("reasoning") {
                        put("effort", request.extraParams["reasoning_effort"] ?: "high")
                        put("exclude", false)
                    }
                }
                if (shouldUseThinkingObject(request.model, config.baseUrl)) {
                    putJsonObject("thinking") {
                        put("type", "enabled")
                    }
                }
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
        val message = obj["choices"]?.jsonArray
            ?.firstOrNull()?.jsonObject
            ?.get("message")?.jsonObject
        val content = contentText(message?.get("content")) ?: firstString(message, "text") ?: ""
        val reasoning = firstReasoning(message, "reasoning_content", "reasoning", "thinking", "reasoning_details")
            ?: firstReasoning(obj, "reasoning_content", "reasoning", "thinking", "reasoning_details")
        val usage = obj["usage"]?.jsonObject?.let {
            TokenUsage(
                promptTokens = it["prompt_tokens"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0,
                completionTokens = it["completion_tokens"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0,
                totalTokens = it["total_tokens"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0
            )
        }
        ChatResponse(content = content, reasoningContent = reasoning, usage = usage)
    } catch (e: Exception) {
        ChatResponse("", error = "解析失败: ${e.message}")
    }

    private fun shouldUseReasoningEffort(model: String, baseUrl: String): Boolean {
        val name = model.lowercase()
        val url = baseUrl.lowercase()
        return listOf("api.openai.com", "openai.azure.com").any { it in url } ||
            listOf("gpt-5", "o1", "o3", "o4", "reasoner", "deepseek-v4").any { it in name }
    }

    private fun shouldUseReasoningObject(model: String, baseUrl: String): Boolean {
        val name = model.lowercase()
        val url = baseUrl.lowercase()
        return listOf("openrouter", "fastrouter", "nanogpt").any { it in url } ||
            listOf("reasoner", "r1", "qwen3", "thinking").any { it in name }
    }

    private fun shouldUseThinkingObject(model: String, baseUrl: String): Boolean {
        val name = model.lowercase()
        val url = baseUrl.lowercase()
        return "deepseek" in url && listOf("deepseek-v4", "deepseek-chat").any { it in name }
    }

    private fun firstString(obj: JsonObject?, vararg keys: String): String? {
        if (obj == null) return null
        return keys.firstNotNullOfOrNull { key -> stringValue(obj[key])?.takeIf { it.isNotBlank() } }
    }

    private fun firstReasoning(obj: JsonObject?, vararg keys: String): String? {
        if (obj == null) return null
        return keys.firstNotNullOfOrNull { key -> visibleReasoning(obj[key])?.takeIf { it.isNotBlank() } }
    }

    private fun contentText(element: JsonElement?): String? {
        if (element == null || element is JsonNull) return null
        return when (element) {
            is JsonPrimitive -> runCatching { element.content }.getOrNull()
            is JsonArray -> element.mapNotNull { item ->
                when (item) {
                    is JsonObject -> {
                        val type = stringValue(item["type"]).orEmpty()
                        if (type.isBlank() || type == "text" || type == "output_text") {
                            contentText(item["text"]) ?: contentText(item["content"])
                        } else {
                            null
                        }
                    }
                    else -> contentText(item)
                }
            }.joinToString("\n").takeIf { it.isNotBlank() }
            is JsonObject -> listOf("text", "content", "output_text")
                .firstNotNullOfOrNull { key -> contentText(element[key])?.takeIf { it.isNotBlank() } }
            else -> null
        }?.trim()?.takeIf { it.isNotBlank() }
    }

    private fun visibleReasoning(element: JsonElement?): String? {
        if (element == null || element is JsonNull) return null
        return when (element) {
            is JsonPrimitive -> runCatching { element.content }.getOrNull()
            is JsonArray -> element.mapNotNull { visibleReasoning(it) }
                .joinToString("\n")
                .takeIf { it.isNotBlank() }
            is JsonObject -> {
                val direct = listOf("summary", "text", "reasoning_content", "reasoning", "thinking", "content")
                    .firstNotNullOfOrNull { key -> visibleReasoning(element[key])?.takeIf { it.isNotBlank() } }
                direct ?: visibleReasoning(element["reasoning_details"])
                    ?: visibleReasoning(element["details"])
            }
            else -> null
        }?.trim()?.takeIf { it.isNotBlank() }
    }

    private fun stringValue(element: JsonElement?): String? {
        if (element == null || element is JsonNull) return null
        return runCatching { element.jsonPrimitive.content }.getOrElse { element.toString() }
    }
}
