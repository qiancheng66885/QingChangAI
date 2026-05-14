package com.aiaggregator.app.data.model

import kotlinx.serialization.Serializable

/**
 * 聊天消息 — 纯数据类，JSON 持久化。
 */
@Serializable
data class Message(
    val id: String,
    val sessionId: String,
    val role: MessageRole,
    val content: String,
    @Deprecated("Migrated to imageUrls", ReplaceWith("imageUrls"))
    val imageUrl: String? = null,       // ← 旧数据兼容，读取后迁移至 imageUrls
    val imageUrls: List<String> = emptyList(),
    val contentType: ContentType = ContentType.TEXT,
    val assetIds: List<String> = emptyList(),
    val parentMessageId: String? = null,
    val timestamp: Long,
    val status: MessageStatus = MessageStatus.DONE,
    val tokenCount: Int? = null,
    val promptTokens: Int? = null,
    val completionTokens: Int? = null,
    val modelName: String? = null,
    val errorMessage: String? = null,
    val reasoningContent: String? = null,
    val metadata: String? = null
) {
    /** 合并新旧字段，优先取 imageUrls */
    val allImageUrls: List<String>
        get() = if (imageUrls.isNotEmpty()) imageUrls
                else imageUrl?.let { listOf(it) } ?: emptyList()
}

@Serializable
enum class MessageRole {
    USER, ASSISTANT, SYSTEM
}

@Serializable
enum class ContentType {
    TEXT, IMAGE, FILE, ERROR
}

@Serializable
enum class MessageStatus {
    SENDING, STREAMING, DONE, ERROR
}
