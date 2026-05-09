package com.aiaggregator.app.base.ext

import java.util.UUID

/**
 * String 扩展函数集合。
 */

/** 生成 UUID 字符串 */
fun String.Companion.uuid(): String = UUID.randomUUID().toString()

/** 判断字符串是否为空白（null 或全空格） */
fun String?.isBlankOrNull(): Boolean = this == null || this.isBlank()

/** 截断字符串到指定长度，超出部分用省略号 */
fun String.truncate(maxLength: Int, ellipsis: String = "..."): String {
    if (this.length <= maxLength) return this
    return this.take(maxLength - ellipsis.length) + ellipsis
}

/** 提取字符串的第一行（用于会话标题生成） */
fun String.firstLine(): String = this.lines().firstOrNull { it.isNotBlank() } ?: "新对话"

/** 脱敏 API 密钥：只显示前4位和后4位 */
fun String.maskApiKey(): String {
    if (this.length <= 8) return "****"
    return "${this.take(4)}****${this.takeLast(4)}"
}

/** 判断字符串是否为有效的 URL */
fun String.isValidUrl(): Boolean {
    return try {
        val uri = java.net.URI(this)
        uri.scheme != null && uri.host != null
    } catch (e: Exception) {
        false
    }
}
