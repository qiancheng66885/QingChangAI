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
import okhttp3.Response
import com.aiaggregator.app.base.utils.LogUtil
import java.io.IOException

class AnthropicAdapter : AiAdapter {

    override val formatType = "anthropic-compatible"

    private val sseClient = HttpClientFactory.sseClient
    private val restClient = HttpClientFactory.client
    private val json = Json { ignoreUnknownKeys = true }
    private enum class ThinkingMode { NONE, MANUAL, ADAPTIVE }

    override fun streamChat(request: ChatRequest, config: ApiConfig): Flow<ChatChunk> = callbackFlow {
        var doneSent = false
        var inputTokens = 0
        try {
            val body = buildJsonObject {
                val thinkingMode = if (request.extraParams["deep_think"] == "true") thinkingMode(request.model) else ThinkingMode.NONE
                put("model", request.model)
                put("stream", true)
                put("max_tokens", request.maxTokens)
                when (thinkingMode) {
                    ThinkingMode.ADAPTIVE -> {
                        putJsonObject("thinking") {
                            put("type", "adaptive")
                            put("display", "summarized")
                        }
                        putJsonObject("output_config") {
                            put("effort", request.extraParams["reasoning_effort"] ?: "high")
                        }
                    }
                    ThinkingMode.MANUAL -> {
                        putJsonObject("thinking") {
                            put("type", "enabled")
                            put("budget_tokens", thinkingBudget(request))
                        }
                    }
                    ThinkingMode.NONE -> put("temperature", request.temperature)
                }

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

            val (endpoint, isCustomEp) = AdapterUtils.effectiveChatEndpoint(config)
            val (authName, authValue) = AdapterUtils.buildAuthHeader(config.apiKey, config.formatType, config.authHeaderName)
            val httpRequest = Request.Builder()
                .url(AdapterUtils.buildUrl(config.baseUrl, endpoint, isCustomEp))
                .header(authName, authValue)
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
                                val reasoning = firstReasoning(delta, "thinking", "reasoning_content", "reasoning", "reasoning_details")
                                if (!reasoning.isNullOrBlank()) {
                                    trySend(ChatChunk(content = null, reasoningContent = reasoning))
                                }
                                val text = firstString(delta, "text")
                                if (!text.isNullOrBlank()) {
                                    trySend(ChatChunk(content = text))
                                }
                            }
                            "message_delta" -> {
                                val delta = obj["delta"]?.jsonObject
                                val stopReason = delta?.get("stop_reason")?.jsonPrimitive?.content
                                if (stopReason != null) {
                                    val outputTokens = obj["usage"]?.jsonObject
                                        ?.get("output_tokens")?.jsonPrimitive?.content?.toIntOrNull() ?: 0
                                    if (!doneSent) { doneSent = true; trySend(ChatChunk(content = null, isDone = true,
                                        usage = TokenUsage(inputTokens, outputTokens, inputTokens + outputTokens))) }
                                    close()
                                }
                            }
                            "message_stop" -> {
                                if (!doneSent) { doneSent = true; trySend(ChatChunk(content = null, isDone = true)) }
                                close()
                            }
                            "error" -> {
                                val errorMsg = obj["error"]?.jsonObject
                                    ?.get("message")?.jsonPrimitive?.content ?: "未知错误"
                                if (!doneSent) { doneSent = true; trySend(ChatChunk(content = null, isDone = true, error = errorMsg)) }
                                close()
                            }
                        }
                    } catch (e: Exception) {
                        LogUtil.w("AnthropicSSE", "SSE parse error: ${data.take(100)}", e)
                        if (!doneSent) { doneSent = true; trySend(ChatChunk(content = null, isDone = true, error = "SSE 解析错误: ${e.message}")) }
                        close()
                    }
                }

                override fun onClosed(es: EventSource) {
                    if (!doneSent) { doneSent = true; trySend(ChatChunk(content = null, isDone = true)) }
                    close()
                }

                override fun onFailure(es: EventSource, t: Throwable?, response: Response?) {
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
            val thinkingMode = if (request.extraParams["deep_think"] == "true") thinkingMode(request.model) else ThinkingMode.NONE
            put("model", request.model)
            put("stream", false)
            put("max_tokens", request.maxTokens)
            when (thinkingMode) {
                ThinkingMode.ADAPTIVE -> {
                    putJsonObject("thinking") {
                        put("type", "adaptive")
                        put("display", "summarized")
                    }
                    putJsonObject("output_config") {
                        put("effort", request.extraParams["reasoning_effort"] ?: "high")
                    }
                }
                ThinkingMode.MANUAL -> {
                    putJsonObject("thinking") {
                        put("type", "enabled")
                        put("budget_tokens", thinkingBudget(request))
                    }
                }
                ThinkingMode.NONE -> put("temperature", request.temperature)
            }

            val systemMsg = request.messages.find { it.role == "system" }
            if (systemMsg != null) put("system", systemMsg.content)

            putJsonArray("messages") {
                request.messages.filter { it.role != "system" }.forEach { msg ->
                    add(buildAnthropicMessage(msg))
                }
            }
        }

        val (endpoint, isCustomEp) = AdapterUtils.effectiveChatEndpoint(config)
        val (authName, authValue) = AdapterUtils.buildAuthHeader(config.apiKey, config.formatType, config.authHeaderName)
        val httpRequest = Request.Builder()
            .url(AdapterUtils.buildUrl(config.baseUrl, endpoint, isCustomEp))
            .header(authName, authValue)
            .header("anthropic-version", "2023-06-01")
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

    private fun buildAnthropicMessage(msg: ChatMessageItem) = buildJsonObject {
        put("role", msg.role)
        putJsonArray("content") {
            if (msg.content.isNotBlank()) {
                add(buildJsonObject { put("type", "text"); put("text", msg.content) })
            }
            if (msg.role == "user") {
                msg.images.forEach { img ->
                    add(buildJsonObject {
                        put("type", "image")
                        putJsonObject("source") {
                            put("type", "base64")
                            put("media_type", img.mimeType)
                            put("data", img.base64)
                        }
                    })
                }
            }
        }
    }

    private fun parseResponse(body: String): ChatResponse = try {
        val obj = json.parseToJsonElement(body).jsonObject
        val contentBlocks = obj["content"]?.jsonArray.orEmpty()
        val content = contentBlocks.mapNotNull { block ->
            val item = block.jsonObject
            val type = item["type"]?.jsonPrimitive?.content
            if (type == null || type == "text") firstString(item, "text") else null
        }.joinToString("\n")
        val reasoning = contentBlocks.mapNotNull { block ->
            val item = block.jsonObject
            val type = item["type"]?.jsonPrimitive?.content
            if (type in setOf("thinking", "redacted_thinking", "reasoning", "reasoning_content")) {
                firstReasoning(item, "thinking", "reasoning_content", "reasoning", "reasoning_details", "text")
            } else {
                null
            }
        }.joinToString("\n").takeIf { it.isNotBlank() }
        val usage = obj["usage"]?.jsonObject?.let {
            val prompt = it["input_tokens"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0
            val completion = it["output_tokens"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0
            TokenUsage(promptTokens = prompt, completionTokens = completion, totalTokens = prompt + completion)
        }
        ChatResponse(content = content, reasoningContent = reasoning, usage = usage)
    } catch (e: Exception) {
        ChatResponse("", error = "解析失败: ${e.message}")
    }

    private fun thinkingBudget(request: ChatRequest): Int {
        val requested = request.extraParams["thinking_budget_tokens"]?.toIntOrNull() ?: 1024
        val maxBudget = (request.maxTokens - 1).coerceAtLeast(128)
        return requested.coerceIn(128, maxBudget)
    }

    private fun thinkingMode(model: String): ThinkingMode {
        val name = model.lowercase()
        if (listOf("opus-4-7", "opus-4.7", "opus-4-6", "opus-4.6", "sonnet-4-6", "sonnet-4.6", "mythos").any { it in name }) {
            return ThinkingMode.ADAPTIVE
        }
        return if (listOf(
            "claude-3-7",
            "claude-3.7",
            "claude-4",
            "sonnet-4",
            "opus-4",
            "haiku-4",
            "mythos"
        ).any { it in name }) ThinkingMode.MANUAL else ThinkingMode.NONE
    }

    private fun firstString(obj: JsonObject?, vararg keys: String): String? {
        if (obj == null) return null
        return keys.firstNotNullOfOrNull { key -> stringValue(obj[key])?.takeIf { it.isNotBlank() } }
    }

    private fun firstReasoning(obj: JsonObject?, vararg keys: String): String? {
        if (obj == null) return null
        return keys.firstNotNullOfOrNull { key -> visibleReasoning(obj[key])?.takeIf { it.isNotBlank() } }
    }

    private fun visibleReasoning(element: JsonElement?): String? {
        if (element == null || element is JsonNull) return null
        return when (element) {
            is JsonPrimitive -> runCatching { element.content }.getOrNull()
            is JsonArray -> element.mapNotNull { visibleReasoning(it) }
                .joinToString("\n")
                .takeIf { it.isNotBlank() }
            is JsonObject -> {
                val direct = listOf("summary", "text", "thinking", "reasoning_content", "reasoning", "content")
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
