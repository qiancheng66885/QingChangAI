package com.aiaggregator.app.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.aiaggregator.app.base.constants.StorageConstants
import com.aiaggregator.app.base.utils.LogUtil
import com.aiaggregator.app.data.model.ApiConfig
import com.aiaggregator.app.data.model.ModelConfig
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * API 密钥存储 — 使用 EncryptedSharedPreferences（AES-256-GCM + Android Keystore）。
 */
class ApiKeyStore(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        StorageConstants.ENCRYPTED_PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    // === 平台 ===

    fun savePlatform(config: ApiConfig) {
        val list = loadPlatforms().toMutableList()
        val idx = list.indexOfFirst { it.id == config.id }
        if (idx >= 0) list[idx] = config else list.add(config)
        prefs.edit().putString(StorageConstants.KEY_PLATFORMS, json.encodeToString(list)).apply()
    }

    fun loadPlatforms(): List<ApiConfig> {
        val str = prefs.getString(StorageConstants.KEY_PLATFORMS, null) ?: return emptyList()
        return try { json.decodeFromString<List<ApiConfig>>(str) } catch (e: Exception) {
            LogUtil.e("ApiKeyStore", "平台配置数据损坏，已重置", e)
            emptyList()
        }
    }

    fun deletePlatform(id: String) {
        val platforms = loadPlatforms().filter { it.id != id }
        val models = loadModels().filter { it.platformId != id }
        prefs.edit()
            .putString(StorageConstants.KEY_PLATFORMS, json.encodeToString(platforms))
            .putString(StorageConstants.KEY_MODELS, json.encodeToString(models))
            .apply()
    }

    // === 模型 ===

    fun saveModel(model: ModelConfig) {
        val list = loadModels().toMutableList()
        val idx = list.indexOfFirst { it.id == model.id }
        if (idx >= 0) list[idx] = model else list.add(model)
        prefs.edit().putString(StorageConstants.KEY_MODELS, json.encodeToString(list)).apply()
    }

    /** 批量保存整个模型列表（单次写入） */
    fun saveAllModels(models: List<ModelConfig>) {
        prefs.edit().putString(StorageConstants.KEY_MODELS, json.encodeToString(models)).apply()
    }

    fun loadModels(): List<ModelConfig> {
        val str = prefs.getString(StorageConstants.KEY_MODELS, null) ?: return emptyList()
        return try { json.decodeFromString<List<ModelConfig>>(str) } catch (e: Exception) {
            LogUtil.e("ApiKeyStore", "模型配置数据损坏，已重置", e)
            emptyList()
        }
    }

    fun loadModelsForPlatform(platformId: String): List<ModelConfig> =
        loadModels().filter { it.platformId == platformId }

    fun deleteModel(id: String) {
        val list = loadModels().filter { it.id != id }
        prefs.edit().putString(StorageConstants.KEY_MODELS, json.encodeToString(list)).apply()
    }

    fun getActiveModel(): ModelConfig? = loadModels().find { it.isDefault }
}
