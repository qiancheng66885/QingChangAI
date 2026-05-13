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
import java.io.IOException

class OpenAiImageGenAdapter : ImageGenAdapter {

    private val client = HttpClientFactory.client
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun generate(request: ImageGenRequest, config: ApiConfig): ImageGenResult {
        val body = buildJsonObject {
            put("model", request.model)
            put("prompt", request.prompt)
            put("n", request.n)
            put("size", request.size)
            put("quality", request.quality)
            request.outputFormat?.let { put("output_format", it) }
            request.background?.let { put("background", it) }
            request.moderation?.let { put("moderation", it) }
            request.outputCompression?.let { put("output_compression", it) }
            request.seed?.let { put("seed", it) }
            request.thinking?.let { put("thinking", it) }
        }

        val (genEndpoint, isCustomGen) = AdapterUtils.effectiveImageGenEndpoint(config)
        val (authName, authValue) = AdapterUtils.buildAuthHeader(config.apiKey, config.formatType, config.authHeaderName)
        val httpRequest = Request.Builder()
            .url(AdapterUtils.buildUrl(config.baseUrl, genEndpoint, isCustomGen))
            .header(authName, authValue)
            .header("Content-Type", "application/json")
            .post(body.toString().toRequestBody("application/json".toMediaType()))
            .build()

        return executeRequest(httpRequest)
    }

    override suspend fun edit(request: ImageEditRequest, config: ApiConfig): ImageGenResult {
        val builder = MultipartBody.Builder().setType(MultipartBody.FORM)

        builder.addFormDataPart("model", request.model)
        builder.addFormDataPart("prompt", request.prompt)
        builder.addFormDataPart("n", request.n.toString())
        builder.addFormDataPart("size", request.size)
        request.outputFormat?.let { builder.addFormDataPart("output_format", it) }
        request.background?.let { builder.addFormDataPart("background", it) }
        request.moderation?.let { builder.addFormDataPart("moderation", it) }
        request.outputCompression?.let { builder.addFormDataPart("output_compression", it.toString()) }
        request.seed?.let { builder.addFormDataPart("seed", it.toString()) }
        request.thinking?.let { builder.addFormDataPart("thinking", it) }
        request.inputFidelity?.let { builder.addFormDataPart("input_fidelity", it) }

        request.images.forEachIndexed { idx, img ->
            val ext = when {
                img.mimeType.contains("png") -> "png"
                img.mimeType.contains("webp") -> "webp"
                img.mimeType.contains("gif") -> "gif"
                else -> "jpg"
            }
            builder.addFormDataPart(
                "image", "image_${idx}.$ext",
                img.bytes.toRequestBody(img.mimeType.toMediaType())
            )
        }

        val (editEndpoint, isCustomEdit) = AdapterUtils.effectiveImageEditEndpoint(config)
        val (authName, authValue) = AdapterUtils.buildAuthHeader(config.apiKey, config.formatType, config.authHeaderName)
        val httpRequest = Request.Builder()
            .url(AdapterUtils.buildUrl(config.baseUrl, editEndpoint, isCustomEdit))
            .header(authName, authValue)
            .post(builder.build())
            .build()

        return executeRequest(httpRequest)
    }

    private fun executeRequest(httpRequest: Request): ImageGenResult {
        return try {
            client.newCall(httpRequest).execute().use { response ->
                if (!response.isSuccessful) {
                    val respBody = response.body?.string() ?: ""
                    return ImageGenResult(error = AdapterUtils.errorDetail(response, respBody))
                }
                AdapterUtils.validateJsonContentType(response)
                val respBody = response.body?.string() ?: ""
                val obj = json.parseToJsonElement(respBody).jsonObject
                val dataArray = obj["data"]?.jsonArray
                val urls = mutableListOf<String>()
                val base64List = mutableListOf<String>()
                if (dataArray != null) {
                    for (item in dataArray) {
                        val it = item.jsonObject
                        it["url"]?.jsonPrimitive?.content?.let { u -> if (u.isNotBlank()) urls.add(u) }
                        it["b64_json"]?.jsonPrimitive?.content?.let { b -> if (b.isNotBlank()) base64List.add(b) }
                    }
                }
                ImageGenResult(urls = urls, base64Images = base64List)
            }
        } catch (e: IOException) {
            ImageGenResult(error = friendlyImageNetworkError(e))
        } catch (e: Exception) {
            ImageGenResult(error = e.message ?: "未知错误")
        }
    }

    private fun friendlyImageNetworkError(e: IOException): String {
        val msg = e.message.orEmpty()
        return when {
            msg.contains("Software caused connection abort", ignoreCase = true) ->
                "网络连接被系统或网络环境中断，常见于退后台、锁屏、切换网络或省电限制。请保持前台和网络稳定后重新生成。"
            msg.contains("timeout", ignoreCase = true) || msg.contains("timed out", ignoreCase = true) ->
                "图片生成超时。可能是上游生成较慢或中转站响应超时，请稍后重新生成。"
            msg.contains("Canceled", ignoreCase = true) ->
                "图片生成请求已被取消。"
            msg.isNotBlank() -> "图片生成网络异常：$msg"
            else -> "图片生成网络异常，请检查网络后重试。"
        }
    }
}
