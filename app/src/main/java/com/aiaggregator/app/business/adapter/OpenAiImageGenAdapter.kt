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

        val httpRequest = Request.Builder()
            .url("${config.baseUrl.trimEnd('/').removeSuffix("/v1")}/v1/images/generations")
            .header("Authorization", "Bearer ${config.apiKey}")
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

        val httpRequest = Request.Builder()
            .url("${config.baseUrl.trimEnd('/').removeSuffix("/v1")}/v1/images/edits")
            .header("Authorization", "Bearer ${config.apiKey}")
            .post(builder.build())
            .build()

        return executeRequest(httpRequest)
    }

    private fun executeRequest(httpRequest: Request): ImageGenResult {
        return try {
            val response = client.newCall(httpRequest).execute()
            val respBody = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                return ImageGenResult(error = "HTTP ${response.code}: ${AdapterUtils.errorBodySummary(respBody)}")
            }
            AdapterUtils.validateJsonContentType(response)
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
        } catch (e: IOException) {
            ImageGenResult(error = e.message ?: "网络连接失败")
        } catch (e: Exception) {
            ImageGenResult(error = e.message ?: "未知错误")
        }
    }
}
