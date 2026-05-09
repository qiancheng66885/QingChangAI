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
    val imageUrl: String? = null,
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
    val metadata: String? = null
)

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
