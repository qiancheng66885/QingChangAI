package com.aiaggregator.app.ui.settings

import com.aiaggregator.app.data.remote.HttpClientFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.Request

@Serializable
data class GiteeRelease(val tag_name: String = "", val name: String = "")

object UpdateChecker {

    private val client = HttpClientFactory.client
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun check(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("https://gitee.com/api/v5/repos/qiancheng2025/QingChangAI/releases/latest")
                    .header("Accept", "application/json")
                    .build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@withContext Result(error = "网络异常")
                    val body = response.body?.string() ?: return@withContext Result(error = "获取失败")
                    val release = json.decodeFromString<GiteeRelease>(body)
                    val latestTag = release.tag_name.removePrefix("v").trim()
                    if (latestTag.isBlank()) return@withContext Result(error = "未发布版本")

                    val current = getAppVersion().trim()
                    if (latestTag != current) {
                        Result(
                            hasUpdate = true,
                            latestVersion = latestTag,
                            downloadUrl = "https://gitee.com/qiancheng2025/QingChangAI/releases/tag/${release.tag_name}"
                        )
                    } else {
                        Result()
                    }
                }
            } catch (_: Exception) {
                Result(error = "网络异常，请稍后重试")
            }
        }
    }

    fun getAppVersion(): String = "1.2.0"

    data class Result(
        val hasUpdate: Boolean = false,
        val latestVersion: String = "",
        val downloadUrl: String = "",
        val error: String? = null
    )
}
