package com.aiaggregator.app.ui.config

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.aiaggregator.app.data.local.ApiKeyStore
import com.aiaggregator.app.data.model.ApiConfig
import com.aiaggregator.app.data.model.ModelConfig

class ConfigViewModel(application: Application) : AndroidViewModel(application) {

    private val store = ApiKeyStore(application)

    fun loadPlatforms(): List<ApiConfig> = store.loadPlatforms()
    fun savePlatform(config: ApiConfig) = store.savePlatform(config)
    fun deletePlatform(id: String) = store.deletePlatform(id)

    fun loadModels(): List<ModelConfig> = store.loadModels()
    fun loadModelsForPlatform(pid: String): List<ModelConfig> = store.loadModelsForPlatform(pid)
    fun saveModel(model: ModelConfig) = store.saveModel(model)
    fun deleteModel(id: String) = store.deleteModel(id)
    fun setDefaultModel(id: String) {
        store.loadModels().map { it.copy(isDefault = it.id == id) }.forEach { store.saveModel(it) }
    }
    fun getActiveModel(): ModelConfig? = store.getActiveModel()
    fun getPlatform(id: String): ApiConfig? = store.loadPlatforms().find { it.id == id }
}
