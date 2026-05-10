package com.aiaggregator.app.business.adapter

import okhttp3.Response
import java.io.IOException

object AdapterUtils {

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
