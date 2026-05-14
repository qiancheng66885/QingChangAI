package com.aiaggregator.app.ui.config

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aiaggregator.app.data.local.ApiKeyStore
import com.aiaggregator.app.data.model.ApiConfig
import com.aiaggregator.app.data.model.ModelCategory
import com.aiaggregator.app.data.model.ModelConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

data class ConfigState(
    val platforms: List<ApiConfig> = emptyList(),
    val models: List<ModelConfig> = emptyList(),
    val isLoading: Boolean = true
)

class ConfigViewModel(application: Application) : AndroidViewModel(application) {

    private val store = ApiKeyStore(application)
    private val _state = MutableStateFlow(ConfigState())
    val state: StateFlow<ConfigState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val snapshot = withContext(Dispatchers.IO) {
                val loadedPlatforms = store.loadPlatforms()
                val rawModels = store.loadModels()
                val models = rawModels.normalizedPickerSelection()
                if (models != rawModels) {
                    store.saveAllModels(models)
                }
                ConfigState(
                    platforms = loadedPlatforms,
                    models = models,
                    isLoading = false
                )
            }
            _state.value = snapshot
        }
    }

    suspend fun upsertConfig(platform: ApiConfig, model: ModelConfig) = withContext(Dispatchers.IO) {
        val current = _state.value
        val platforms = current.platforms.upsertById(platform) { it.id }
        val models = current.models.upsertById(model) { it.id }.normalizedPickerSelection()
        store.saveConfigSnapshot(platforms, models)
        _state.value = ConfigState(platforms, models, isLoading = false)
    }

    suspend fun setDefaultModel(id: String): Boolean = withContext(Dispatchers.IO) {
        val current = _state.value
        val target = current.models.find { it.id == id } ?: return@withContext false
        if (target.category == ModelCategory.IMAGE) return@withContext false
        val models = current.models.map {
            it.copy(
                isDefault = it.id == id,
                showInPicker = if (it.category == target.category) it.id == id else it.showInPicker
            )
        }.normalizedPickerSelection()
        store.saveAllModels(models)
        _state.value = current.copy(models = models, isLoading = false)
        true
    }

    suspend fun setCategoryPickerModel(id: String): Boolean = withContext(Dispatchers.IO) {
        val current = _state.value
        val target = current.models.find { it.id == id } ?: return@withContext false
        val models = current.models.map {
            if (it.category == target.category) it.copy(showInPicker = it.id == id) else it
        }.normalizedPickerSelection()
        store.saveAllModels(models)
        _state.value = current.copy(models = models, isLoading = false)
        true
    }

    suspend fun deleteModel(id: String) = withContext(Dispatchers.IO) {
        val current = _state.value
        val models = current.models.filter { it.id != id }.normalizedPickerSelection()
        store.saveAllModels(models)
        _state.value = current.copy(models = models, isLoading = false)
    }

    suspend fun duplicateModel(id: String): Pair<ModelConfig, ApiConfig>? = withContext(Dispatchers.IO) {
        val current = _state.value
        val model = current.models.find { it.id == id } ?: return@withContext null
        val platform = current.platforms.find { it.id == model.platformId } ?: return@withContext null
        val baseName = model.displayName.ifBlank { model.modelName }
        val pattern = Regex("""^${Regex.escape(baseName)} 副本(\d+)$""")
        val maxN = current.models.mapNotNull { other ->
            pattern.find(other.displayName)?.groupValues?.get(1)?.toIntOrNull()
        }.maxOrNull() ?: 0
        val newPlatform = platform.copy(id = UUID.randomUUID().toString())
        val newModel = model.copy(
            id = UUID.randomUUID().toString(),
            platformId = newPlatform.id,
            displayName = "$baseName 副本${maxN + 1}",
            isDefault = false,
            showInPicker = false
        )
        val platforms = current.platforms + newPlatform
        val models = (current.models + newModel).normalizedPickerSelection()
        store.saveConfigSnapshot(platforms, models)
        _state.value = ConfigState(platforms, models, isLoading = false)
        newModel to newPlatform
    }

    fun loadPlatforms(): List<ApiConfig> = state.value.platforms.ifEmpty { store.loadPlatforms() }
    fun savePlatform(config: ApiConfig) = store.savePlatform(config).also { refresh() }
    fun deletePlatform(id: String) = store.deletePlatform(id).also { refresh() }

    fun loadModels(): List<ModelConfig> = state.value.models.ifEmpty { store.loadModels() }
    fun loadModelsForPlatform(pid: String): List<ModelConfig> = loadModels().filter { it.platformId == pid }
    fun saveModel(model: ModelConfig) = store.saveModel(model).also { refresh() }
    fun deleteModelSync(id: String) = store.deleteModel(id).also { refresh() }
    fun getActiveModel(): ModelConfig? =
        state.value.models.find { it.isDefault && it.category != ModelCategory.IMAGE }
            ?: store.getActiveModel()
    fun getPlatform(id: String): ApiConfig? = state.value.platforms.find { it.id == id }

    private inline fun <T, K> List<T>.upsertById(item: T, key: (T) -> K): List<T> {
        val idx = indexOfFirst { key(it) == key(item) }
        return if (idx >= 0) toMutableList().also { it[idx] = item } else this + item
    }

    private fun List<ModelConfig>.normalizedPickerSelection(): List<ModelConfig> {
        val defaultId = firstOrNull { it.isDefault && it.category != ModelCategory.IMAGE }?.id
        val selectedIds = groupBy { it.category }.values.mapNotNull { group ->
            group.firstOrNull { it.showInPicker }?.id ?: group.firstOrNull()?.id
        }.toSet()
        return map {
            it.copy(
                isDefault = it.id == defaultId,
                showInPicker = it.id in selectedIds
            )
        }
    }
}
