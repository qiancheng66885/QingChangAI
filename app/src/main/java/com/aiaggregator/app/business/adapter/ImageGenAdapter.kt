package com.aiaggregator.app.business.adapter

import com.aiaggregator.app.data.model.ApiConfig

data class ImageGenRequest(
    val model: String,
    val prompt: String,
    val size: String = "1024x1024",
    val quality: String = "standard",
    val n: Int = 1
)

data class ImageEditRequest(
    val model: String,
    val prompt: String,
    val imageBytes: ByteArray,
    val size: String = "1024x1024",
    val n: Int = 1
)

data class ImageGenResult(
    val urls: List<String> = emptyList(),
    val revisedPrompt: String? = null,
    val error: String? = null
)

interface ImageGenAdapter {
    suspend fun generate(request: ImageGenRequest, config: ApiConfig): ImageGenResult
    suspend fun edit(request: ImageEditRequest, config: ApiConfig): ImageGenResult
}
