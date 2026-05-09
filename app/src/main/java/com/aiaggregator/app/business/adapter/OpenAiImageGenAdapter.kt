package com.aiaggregator.app.business.adapter

import com.aiaggregator.app.data.model.ApiConfig
import com.aiaggregator.app.data.remote.HttpClientFactory
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

class OpenAiImageGenAdapter : ImageGenAdapter {

    private val client = HttpClientFactory.create()
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun generate(request: ImageGenRequest, config: ApiConfig): ImageGenResult {
        val body = buildJsonObject {
            put("model", request.model)
            put("prompt", request.prompt)
            put("n", request.n)
            put("size", request.size)
            put("quality", request.quality)
        }

        val httpRequest = Request.Builder()
            .url("${config.baseUrl.trimEnd('/').removeSuffix("/v1")}/v1/images/generations")
            .header("Authorization", "Bearer ${config.apiKey}")
            .header("Content-Type", "application/json")
            .post(body.toString().toRequestBody("application/json".toMediaType()))
            .build()

        return executeRequest(httpRequest)
    }

    override suspend fun edit(request: ImageEditRequest, config: ApiConfig): ImageGenResult {
        val multipart = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("model", request.model)
            .addFormDataPart("prompt", request.prompt)
            .addFormDataPart("n", request.n.toString())
            .addFormDataPart("size", request.size)
            .addFormDataPart(
                "image", "image.png",
                request.imageBytes.toRequestBody("image/png".toMediaType())
            )
            .build()

        val httpRequest = Request.Builder()
            .url("${config.baseUrl.trimEnd('/').removeSuffix("/v1")}/v1/images/edits")
            .header("Authorization", "Bearer ${config.apiKey}")
            .post(multipart)
            .build()

        return executeRequest(httpRequest)
    }

    private fun executeRequest(httpRequest: Request): ImageGenResult {
        return try {
            val response = client.newCall(httpRequest).execute()
            val respBody = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                return ImageGenResult(error = "HTTP ${response.code}: ${errorBodySummary(respBody)}")
            }
            validateJsonContentType(response)
            val obj = json.parseToJsonElement(respBody).jsonObject
            val urls = obj["data"]?.jsonArray?.map {
                it.jsonObject["url"]?.jsonPrimitive?.content ?: ""
            } ?: emptyList()
            ImageGenResult(urls = urls)
        } catch (e: IOException) {
            ImageGenResult(error = "网络错误: ${e.message}")
        } catch (e: Exception) {
            val detail = if (e.message?.contains("JSON") == true) {
                "服务返回了非 JSON 内容，可能是代理或节点错误: ${e.message}"
            } else {
                e.message ?: "未知错误"
            }
            ImageGenResult(error = "请求失败: $detail")
        }
    }

    private fun validateJsonContentType(response: Response) {
        val ct = response.body?.contentType()
        if (ct != null && (ct.type != "application" || ct.subtype != "json")) {
            val url = response.request.url.toString()
            val preview = response.peekBody(500).string()
            throw IOException("服务器返回了非 JSON 内容 (${ct.type}/${ct.subtype})，请检查 API 地址是否正确。请求 URL: $url，响应预览: $preview")
        }
    }

    private fun errorBodySummary(body: String): String {
        return if (body.length > 200) body.take(200) + "..." else body
    }
}
