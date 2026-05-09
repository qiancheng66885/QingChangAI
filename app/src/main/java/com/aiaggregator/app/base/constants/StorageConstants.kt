package com.aiaggregator.app.base.constants

/**
 * 本地存储相关的常量定义。
 */
object StorageConstants {

    // === Room 数据库 ===
    const val DATABASE_NAME = "aiaggregator.db"
    const val DATABASE_VERSION = 1

    // === DataStore ===
    const val DATASTORE_SETTINGS = "app_settings"
    const val DATASTORE_REMOTE_CONFIG = "remote_config_cache"

    // === EncryptedSharedPreferences ===
    const val ENCRYPTED_PREFS_NAME = "secure_prefs"

    // === 文件存储 ===
    /** 生成的图片/文件的存储子目录 */
    const val ASSETS_DIR = "generated_assets"

    /** 临时缓存目录 */
    const val CACHE_DIR = "temp_cache"

    // === DataStore 键名 ===
    const val KEY_THEME_MODE = "theme_mode"
    const val KEY_LANGUAGE = "language"
    const val KEY_FONT_SIZE = "font_size"
    const val KEY_AUTO_CHECK_UPDATE = "auto_check_update"
    const val KEY_LAST_CONFIG_ETAG = "last_config_etag"
    const val KEY_LAST_CONFIG_FETCH_AT = "last_config_fetch_at"

    // === EncryptedSharedPreferences 键名 ===
    const val KEY_PLATFORMS = "platforms"
    const val KEY_MODELS = "models"
    const val KEY_ENCRYPTED_API_KEYS = "encrypted_api_keys"
}
