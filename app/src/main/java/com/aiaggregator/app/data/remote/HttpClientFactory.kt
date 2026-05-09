package com.aiaggregator.app.data.remote

import com.aiaggregator.app.base.constants.ApiConstants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

/**
 * OkHttp 客户端工厂。
 * 提供统一的 HTTP 客户端配置（超时、日志、拦截器）。
 */
object HttpClientFactory {

    /**
     * 创建标准 HTTP 客户端（用于 REST API 请求）。
     */
    fun create(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(ApiConstants.DEFAULT_CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(ApiConstants.DEFAULT_READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.HEADERS // 不打印 Body（防泄露密钥）
            })
            .build()
    }

    /**
     * 创建 SSE 专用 HTTP 客户端（读取超时更长，因为流式连接可能持续数分钟）。
     */
    fun createForSse(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(ApiConstants.DEFAULT_CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.MILLISECONDS)  // 无超时 — SSE 连接可能很长
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.HEADERS
            })
            .build()
    }
}
