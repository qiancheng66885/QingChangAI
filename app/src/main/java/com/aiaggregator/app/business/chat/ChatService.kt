package com.aiaggregator.app.business.chat

import com.aiaggregator.app.business.adapter.AiAdapter
import com.aiaggregator.app.business.adapter.AnthropicAdapter
import com.aiaggregator.app.business.adapter.ChatChunk
import com.aiaggregator.app.business.adapter.ChatMessageItem
import com.aiaggregator.app.business.adapter.ChatRequest
import com.aiaggregator.app.business.adapter.ChatResponse
import com.aiaggregator.app.business.adapter.ImageGenAdapter
import com.aiaggregator.app.business.adapter.ImageEditRequest
import com.aiaggregator.app.business.adapter.ImageGenRequest
import com.aiaggregator.app.business.adapter.ImageGenResult
import com.aiaggregator.app.business.adapter.ImagePart
import com.aiaggregator.app.business.adapter.OpenAiAdapter
import com.aiaggregator.app.business.adapter.OpenAiImageGenAdapter
import com.aiaggregator.app.data.model.ApiConfig
import com.aiaggregator.app.data.model.ApiFormatType
import kotlinx.coroutines.flow.Flow

class ChatService {
    private val openAiAdapter = OpenAiAdapter()
    private val anthropicAdapter = AnthropicAdapter()
    private val imageGenAdapter = OpenAiImageGenAdapter()

    private fun getAdapter(cfg: ApiConfig): AiAdapter = when (cfg.formatType) {
        ApiFormatType.OPENAI_COMPATIBLE -> openAiAdapter
        ApiFormatType.ANTHROPIC_COMPATIBLE -> anthropicAdapter
        else -> openAiAdapter
    }

    fun streamChat(msgs: List<ChatMessageItem>, cfg: ApiConfig, modelName: String, extraParams: Map<String, String> = emptyMap()): Flow<ChatChunk> =
        getAdapter(cfg).streamChat(ChatRequest(model = modelName, messages = msgs, extraParams = extraParams), cfg)

    suspend fun syncChat(msgs: List<ChatMessageItem>, cfg: ApiConfig, modelName: String, extraParams: Map<String, String> = emptyMap()): ChatResponse =
        getAdapter(cfg).syncChat(ChatRequest(model = modelName, messages = msgs, extraParams = extraParams), cfg)

    suspend fun generateImage(prompt: String, modelName: String, cfg: ApiConfig): ImageGenResult =
        imageGenAdapter.generate(ImageGenRequest(model = modelName, prompt = prompt), cfg)

    suspend fun editImage(prompt: String, modelName: String, images: List<ImagePart>, cfg: ApiConfig): ImageGenResult =
        imageGenAdapter.edit(ImageEditRequest(model = modelName, prompt = prompt, images = images), cfg)
}
