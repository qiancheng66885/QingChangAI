package com.aiaggregator.app.data.remote

import com.aiaggregator.app.base.constants.ApiConstants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

object HttpClientFactory {

    /** Cached singleton — shared across all adapters and callers */
    val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(ApiConstants.DEFAULT_READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .callTimeout(5, TimeUnit.MINUTES) // absolute cap — zombie connections die here
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC // 不记录 header 防泄露 API 密钥
            })
            .build()
    }

    /** Cached singleton — SSE streaming with no read timeout + 5 min call timeout */
    val sseClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(ApiConstants.DEFAULT_CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .callTimeout(5, TimeUnit.MINUTES)
            .writeTimeout(120, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            })
            .build()
    }

    // Keep legacy factory methods for backward compatibility
    @Deprecated("Use client directly", ReplaceWith("client"))
    fun create(): OkHttpClient = client

    @Deprecated("Use sseClient directly", ReplaceWith("sseClient"))
    fun createForSse(): OkHttpClient = sseClient
}
