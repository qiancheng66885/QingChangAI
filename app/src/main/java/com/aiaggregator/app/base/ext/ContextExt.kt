package com.aiaggregator.app.base.ext

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

/**
 * Context 扩展函数集合。
 */

/** 显示短 Toast */
fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

/** 显示长 Toast */
fun Context.showLongToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

fun Context.safeStartActivity(intent: Intent, failureMessage: String = "未找到可打开的应用"): Boolean {
    return try {
        startActivity(intent)
        true
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(this, failureMessage, Toast.LENGTH_SHORT).show()
        false
    } catch (_: SecurityException) {
        Toast.makeText(this, failureMessage, Toast.LENGTH_SHORT).show()
        false
    } catch (_: Exception) {
        Toast.makeText(this, failureMessage, Toast.LENGTH_SHORT).show()
        false
    }
}

/** 在浏览器中打开 URL */
fun Context.openUrl(url: String, failureMessage: String = "无法打开链接") {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    safeStartActivity(intent, failureMessage)
}

/** 分享文本到其他应用 */
fun Context.shareText(text: String, title: String = "分享") {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    safeStartActivity(Intent.createChooser(intent, title), "未找到可分享的应用")
}

/** 分享图片文件到其他应用 */
fun Context.shareImage(file: File, authority: String = "${packageName}.fileprovider") {
    val uri = FileProvider.getUriForFile(this, authority, file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/*"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    safeStartActivity(Intent.createChooser(intent, "分享图片"), "未找到可分享图片的应用")
}
