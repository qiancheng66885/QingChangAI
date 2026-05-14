package com.aiaggregator.app.base.constants

/**
 * API 相关的常量定义。
 * 注意：这里的值是出厂默认值，实际运行时可能被远程配置覆盖。
 */
object ApiConstants {

    /** 默认的远程配置拉取基础地址 */
    const val DEFAULT_CONFIG_BASE_URL = "https://config.aiaggregator.app"

    /** 远程配置拉取端点 */
    const val CONFIG_ENDPOINT = "/api/v1/config"

    /** 版本检查端点 */
    const val VERSION_ENDPOINT = "/api/v1/version"

    /** 匿名统计上报端点 */
    const val ANALYTICS_ENDPOINT = "/api/v1/analytics"

    /** 公告拉取端点 */
    const val ANNOUNCEMENTS_ENDPOINT = "/api/v1/announcements"

    /** 默认网络请求超时（秒） */
    const val DEFAULT_CONNECT_TIMEOUT_SECONDS = 30L

    /** 默认读取超时（秒）— SSE 流式请求可能需要更长时间 */
    const val DEFAULT_READ_TIMEOUT_SECONDS = 300L

    /** 最大重试次数 */
    const val MAX_RETRY_COUNT = 3

    /** 重试间隔基数（毫秒） */
    const val RETRY_BASE_DELAY_MS = 1000L

    /** 远程配置拉取最小间隔（毫秒）— 防止频繁请求 */
    const val CONFIG_FETCH_MIN_INTERVAL_MS = 60 * 60 * 1000L // 1 小时
}
