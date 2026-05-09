package com.aiaggregator.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.aiaggregator.app.base.constants.StorageConstants
import com.aiaggregator.app.data.model.AppLanguage
import com.aiaggregator.app.data.model.AppSettings
import com.aiaggregator.app.data.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = StorageConstants.DATASTORE_SETTINGS
)

/**
 * 用户设置存储 — 基于 DataStore Preferences。
 */
class SettingsStore(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey(StorageConstants.KEY_THEME_MODE)
        val LANGUAGE = stringPreferencesKey(StorageConstants.KEY_LANGUAGE)
        val AUTO_CHECK_UPDATE = booleanPreferencesKey(StorageConstants.KEY_AUTO_CHECK_UPDATE)
        val LAST_CONFIG_ETAG = stringPreferencesKey(StorageConstants.KEY_LAST_CONFIG_ETAG)
        val LAST_CONFIG_FETCH_AT = longPreferencesKey(StorageConstants.KEY_LAST_CONFIG_FETCH_AT)
    }

    /**
     * 流式读取设置。
     */
    val settingsFlow: Flow<AppSettings> = context.settingsDataStore.data.map { prefs ->
        AppSettings(
            themeMode = try {
                ThemeMode.valueOf(prefs[Keys.THEME_MODE] ?: ThemeMode.SYSTEM.name)
            } catch (_: Exception) { ThemeMode.SYSTEM },
            language = try {
                AppLanguage.valueOf(prefs[Keys.LANGUAGE] ?: AppLanguage.SYSTEM.name)
            } catch (_: Exception) { AppLanguage.SYSTEM },
            autoCheckUpdate = prefs[Keys.AUTO_CHECK_UPDATE] ?: true,
            lastConfigEtag = prefs[Keys.LAST_CONFIG_ETAG],
            lastConfigFetchAt = prefs[Keys.LAST_CONFIG_FETCH_AT]
        )
    }

    /**
     * 更新主题模式。
     */
    suspend fun setThemeMode(mode: ThemeMode) {
        context.settingsDataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    /**
     * 更新语言。
     */
    suspend fun setLanguage(lang: AppLanguage) {
        context.settingsDataStore.edit { it[Keys.LANGUAGE] = lang.name }
    }

    /**
     * 更新字体大小。
     */
    /**
     * 更新远程配置元数据（ETag + 拉取时间）。
     */
    suspend fun updateConfigMeta(etag: String?, fetchAt: Long) {
        context.settingsDataStore.edit { prefs ->
            if (etag != null) prefs[Keys.LAST_CONFIG_ETAG] = etag
            prefs[Keys.LAST_CONFIG_FETCH_AT] = fetchAt
        }
    }

    /**
     * 获取上次配置的 ETag（用于条件请求）。
     */
    suspend fun getLastEtag(): String? {
        var etag: String? = null
        context.settingsDataStore.data.collect { prefs ->
            etag = prefs[Keys.LAST_CONFIG_ETAG]
            return@collect // 只取一次
        }
        return etag
    }

    /**
     * 判断距上次配置拉取是否已超过指定间隔。
     */
    suspend fun shouldFetchConfig(minIntervalMs: Long): Boolean {
        var shouldFetch = true
        context.settingsDataStore.data.collect { prefs ->
            val lastFetch = prefs[Keys.LAST_CONFIG_FETCH_AT] ?: 0L
            shouldFetch = (System.currentTimeMillis() - lastFetch) >= minIntervalMs
            return@collect
        }
        return shouldFetch
    }
}
