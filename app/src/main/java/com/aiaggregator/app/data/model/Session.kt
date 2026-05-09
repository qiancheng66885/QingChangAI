package com.aiaggregator.app.data.model

import kotlinx.serialization.Serializable

/**
 * 聊天会话 — 纯数据类，JSON 持久化。
 */
@Serializable
data class Session(
    val id: String,
    val title: String,
    val modelConfigId: String,
    val systemPrompt: String? = null,
    val createdAt: Long,
    val lastActiveAt: Long,
    val isPinned: Boolean = false,
    val messageCount: Int = 0,
    val totalTokens: Long = 0,
    val estimatedCost: Double = 0.0
)
