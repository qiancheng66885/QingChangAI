package com.aiaggregator.app.business.adapter

import com.aiaggregator.app.data.model.ApiConfig
import com.aiaggregator.app.data.model.ApiFormatType
import okhttp3.Response
import java.io.IOException

object AdapterUtils {

    // ---- 默认端点路径 ----

    const val DEFAULT_OPENAI_CHAT_ENDPOINT = "/v1/chat/completions"
    const val DEFAULT_OPENAI_IMAGE_GEN_ENDPOINT = "/v1/images/generations"
    const val DEFAULT_OPENAI_IMAGE_EDIT_ENDPOINT = "/v1/images/edits"
    const val DEFAULT_ANTHROPIC_CHAT_ENDPOINT = "/v1/messages"

    // ---- URL 构建 ----

    /**
     * 构建完整请求 URL。
     * 自定义模式（isCustom=true）：直接 baseUrl + endpoint 拼接。
     * 默认模式（isCustom=false）：走 removeSuffix("/v1") 兼容旧逻辑。
     */
    fun buildUrl(baseUrl: String, endpoint: String, isCustom: Boolean): String {
        return if (isCustom) {
            "${baseUrl.trimEnd('/')}/${endpoint.trimStart('/')}"
        } else {
            "${baseUrl.trimEnd('/').removeSuffix("/v1")}$endpoint"
        }
    }

    // ---- 认证头构建 ----

    fun buildAuthHeader(
        apiKey: String,
        formatType: ApiFormatType,
        customHeaderName: String?
    ): Pair<String, String> {
        val name = customHeaderName ?: when (formatType) {
            ApiFormatType.OPENAI_COMPATIBLE -> "Authorization"
            ApiFormatType.ANTHROPIC_COMPATIBLE -> "x-api-key"
        }
        val prefix = when (formatType) {
            ApiFormatType.OPENAI_COMPATIBLE -> "Bearer "
            ApiFormatType.ANTHROPIC_COMPATIBLE -> ""
        }
        return name to "$prefix$apiKey"
    }

    // ---- 有效端点获取 ----

    /** 返回 (endpoint, isCustom) */
    fun effectiveChatEndpoint(config: ApiConfig): Pair<String, Boolean> {
        val ep = config.chatEndpoint ?: when (config.formatType) {
            ApiFormatType.OPENAI_COMPATIBLE -> DEFAULT_OPENAI_CHAT_ENDPOINT
            ApiFormatType.ANTHROPIC_COMPATIBLE -> DEFAULT_ANTHROPIC_CHAT_ENDPOINT
        }
        return ep to (config.chatEndpoint != null)
    }

    fun effectiveImageGenEndpoint(config: ApiConfig): Pair<String, Boolean> {
        val ep = config.imageGenEndpoint ?: DEFAULT_OPENAI_IMAGE_GEN_ENDPOINT
        return ep to (config.imageGenEndpoint != null)
    }

    fun effectiveImageEditEndpoint(config: ApiConfig): Pair<String, Boolean> {
        val ep = config.imageEditEndpoint ?: DEFAULT_OPENAI_IMAGE_EDIT_ENDPOINT
        return ep to (config.imageEditEndpoint != null)
    }

    // ---- 错误诊断 ----

    /** 从 HTTP 响应中提取错误详情，包含 URL 和响应体（body 已消费时用此重载） */
    fun errorDetail(response: okhttp3.Response, bodyString: String): String {
        val url = response.request.url.toString()
        val snippet = errorBodySummary(bodyString)
        return if (snippet.isNotBlank()) {
            "HTTP ${response.code} — $snippet\n请求: $url"
        } else {
            "HTTP ${response.code}\n请求: $url"
        }
    }

    /** 从 HTTP 响应中提取错误详情，尝试 peek 响应体 */
    fun errorDetail(response: okhttp3.Response): String {
        val bodyString = try {
            response.peekBody(1024).string()
        } catch (_: Exception) { "" }
        return errorDetail(response, bodyString)
    }

    // ---- 现有工具 ----

    fun validateJsonContentType(response: Response) {
        val ct = response.body?.contentType()
        if (ct != null && (ct.type != "application" || ct.subtype != "json")) {
            val url = response.request.url.toString()
            val preview = response.peekBody(500).string()
            throw IOException("服务器返回了非 JSON 内容 (${ct.type}/${ct.subtype})，请检查 API 地址是否正确。请求 URL: $url，响应预览: $preview")
        }
    }

    fun errorBodySummary(body: String): String {
        return if (body.length > 500) body.take(500) + "..." else body
    }
}
