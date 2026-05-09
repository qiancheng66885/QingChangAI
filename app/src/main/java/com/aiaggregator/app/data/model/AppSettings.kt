package com.aiaggregator.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val language: AppLanguage = AppLanguage.SYSTEM,
    val autoCheckUpdate: Boolean = true,
    val lastConfigEtag: String? = null,
    val lastConfigFetchAt: Long? = null
)

@Serializable
enum class ThemeMode { LIGHT, DARK, SYSTEM }

@Serializable
enum class AppLanguage { CHINESE, ENGLISH, SYSTEM }
