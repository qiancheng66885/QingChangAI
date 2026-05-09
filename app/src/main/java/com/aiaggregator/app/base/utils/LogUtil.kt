package com.aiaggregator.app.base.utils

import android.util.Log

/**
 * 统一日志工具。
 * 生产环境可通过 [isDebug] 控制日志级别；所有日志通过此类输出，便于统一管理和过滤。
 *
 * **安全规则：绝不在此类中打印 API 密钥、Token 等敏感信息。**
 */
object LogUtil {

    private const val TAG_PREFIX = "AIApp"
    private var isDebug = true

    fun setDebug(debug: Boolean) {
        isDebug = debug
    }

    /** Debug — 开发调试信息，Release 版本不输出 */
    fun d(tag: String, message: String) {
        if (isDebug) Log.d("$TAG_PREFIX-$tag", message)
    }

    /** Info — 常规运行信息 */
    fun i(tag: String, message: String) {
        Log.i("$TAG_PREFIX-$tag", message)
    }

    /** Warning — 可恢复的异常情况 */
    fun w(tag: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.w("$TAG_PREFIX-$tag", message, throwable)
        } else {
            Log.w("$TAG_PREFIX-$tag", message)
        }
    }

    /** Error — 不可恢复的错误 */
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e("$TAG_PREFIX-$tag", message, throwable)
        } else {
            Log.e("$TAG_PREFIX-$tag", message)
        }
    }
}
