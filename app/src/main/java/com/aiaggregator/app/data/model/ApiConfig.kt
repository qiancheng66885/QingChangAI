package com.aiaggregator.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 平台配置 — 一个 API 提供商或中转站。
 * 一个平台可以配置多个模型。
 */
@Serializable
data class ApiConfig(
    val id: String = java.util.UUID.randomUUID().toString(),
    val platformName: String = "",
    val baseUrl: String = "",
    @SerialName("apiKeyEncrypted")  // backward compat — stored JSON uses old name
    val apiKey: String = "",
    val formatType: ApiFormatType = ApiFormatType.OPENAI_COMPATIBLE,
    // 自定义端点路径（null = 使用默认值），空字符串视为 null
    val chatEndpoint: String? = null,
    val imageGenEndpoint: String? = null,
    val imageEditEndpoint: String? = null,
    val authHeaderName: String? = null
)

/**
 * 模型配置 — 属于某个平台的单个模型。
 */
@Serializable
data class ModelConfig(
    val id: String = java.util.UUID.randomUUID().toString(),
    val platformId: String = "",
    val displayName: String = "",
    val modelName: String = "",
    val category: ModelCategory = ModelCategory.CHAT,
    val isDefault: Boolean = false
)

@Serializable
enum class ModelCategory(val label: String) {
    CHAT("语言模型"),
    IMAGE("图片生成"),
    VIDEO("视频生成"),
    AUDIO("音频生成"),
    MULTIMODAL("多模态")
}

@Serializable
enum class ApiFormatType(val label: String) {
    OPENAI_COMPATIBLE("OpenAI 兼容"),
    ANTHROPIC_COMPATIBLE("Anthropic 兼容")
}

/** 连通性测试结果 */
@Serializable
enum class TestResult {
    SUCCESS, TIMEOUT, AUTH_FAILED, MODEL_NOT_FOUND, NETWORK_ERROR, UNKNOWN_ERROR
}
