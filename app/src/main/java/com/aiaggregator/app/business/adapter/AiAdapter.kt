package com.aiaggregator.app.business.adapter

import com.aiaggregator.app.data.model.ApiConfig
import com.aiaggregator.app.data.model.TestResult
import kotlinx.coroutines.flow.Flow

/**
 * AI 协议适配器统一接口。
 * 所有新增的 AI API 协议（OpenAI、Anthropic、未来厂商）都必须实现此接口。
 * 调用方只依赖此接口，不依赖具体实现，新增协议无需修改调用方代码。
 */
interface AiAdapter {

    /** 适配器标识（如 "openai-compatible"） */
    val formatType: String

    /**
     * 流式聊天请求。
     * @return Flow<ChatChunk> — 逐块返回 AI 回复内容
     */
    fun streamChat(request: ChatRequest, config: ApiConfig): Flow<ChatChunk>

    /**
     * 非流式聊天请求（一次返回完整结果）。
     */
    suspend fun syncChat(request: ChatRequest, config: ApiConfig): ChatResponse
}

/**
 * 通用聊天请求。
 * 各适配器实现负责转换为自己的 API 格式。
 */
data class ChatRequest(
    /** 模型名称 */
    val model: String,
    /** 消息列表（包含 system prompt + 历史消息 + 当前用户消息） */
    val messages: List<ChatMessageItem>,
    /** Temperature 参数 */
    val temperature: Double = 0.7,
    /** 最大输出 Token 数 */
    val maxTokens: Int = 4096,
    /** 其他扩展参数（JSON 格式） */
    val extraParams: Map<String, String> = emptyMap()
)

/**
 * 聊天消息项。
 */
data class ChatMessageItem(
    val role: String,   // "system" | "user" | "assistant"
    val content: String,
    val imageBase64: String? = null,  // base64-encoded image for multimodal
    val imageMimeType: String? = null // e.g. "image/jpeg", "image/png"
)

/**
 * 流式响应块。
 * SSE 流中每个 data 块解析为一条 Chunk。
 */
data class ChatChunk(
    /** 本次增量文本内容，null 表示流结束 */
    val content: String?,
    /** 是否流结束 */
    val isDone: Boolean = false,
    /** Token 用量（如有），仅在 isDone=true 时有效 */
    val usage: TokenUsage? = null,
    /** 错误信息 */
    val error: String? = null
)

/**
 * 非流式完整响应。
 */
data class ChatResponse(
    val content: String,
    val usage: TokenUsage? = null,
    val error: String? = null
)

/**
 * Token 用量统计。
 */
data class TokenUsage(
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int
)
