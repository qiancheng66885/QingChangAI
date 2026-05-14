package com.aiaggregator.app.business.adapter

import com.aiaggregator.app.data.model.ApiConfig

data class ImagePart(val bytes: ByteArray, val mimeType: String)

data class ImageGenRequest(
    val model: String,
    val prompt: String,
    val size: String? = null,
    val quality: String = "auto",
    val n: Int = 1,
    val outputFormat: String? = null,
    val background: String? = null,
    val moderation: String? = null,
    val outputCompression: Int? = null,
    val seed: Int? = null,
    val thinking: String? = null
)

data class ImageEditRequest(
    val model: String,
    val prompt: String,
    val images: List<ImagePart>,
    val size: String? = null,
    val n: Int = 1,
    val outputFormat: String? = null,
    val background: String? = null,
    val moderation: String? = null,
    val outputCompression: Int? = null,
    val seed: Int? = null,
    val thinking: String? = null,
    val inputFidelity: String? = null
)

data class ImageGenResult(
    val urls: List<String> = emptyList(),
    val base64Images: List<String> = emptyList(),
    val revisedPrompt: String? = null,
    val error: String? = null
)

interface ImageGenAdapter {
    suspend fun generate(request: ImageGenRequest, config: ApiConfig): ImageGenResult
    suspend fun edit(request: ImageEditRequest, config: ApiConfig): ImageGenResult
}
