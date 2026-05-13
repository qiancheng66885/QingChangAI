package com.aiaggregator.app.data.local

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.aiaggregator.app.data.remote.HttpClientFactory
import java.io.File
import java.net.URI

object ImageStorageManager {
    private const val GENERATED_DIR = "images/generated"
    private const val MAX_IMAGE_BYTES = 50L * 1024L * 1024L
    private const val MAX_BASE64_CHARS = ((MAX_IMAGE_BYTES * 4L) / 3L + 8L).toInt()

    fun persistBase64Image(context: Context, base64: String): String? {
        if (base64.length > MAX_BASE64_CHARS) return null
        return try {
            val bytes = android.util.Base64.decode(base64, android.util.Base64.NO_WRAP)
            if (bytes.size > MAX_IMAGE_BYTES) return null
            saveGeneratedImage(context, bytes, "png")
        } catch (_: OutOfMemoryError) {
            null
        } catch (_: Exception) {
            null
        }
    }

    fun persistImageUrl(context: Context, url: String): String? {
        repeat(3) { attempt ->
            val saved = saveImageRefToGeneratedFile(context, url)
            if (saved != null) return saved
            if (attempt < 2) {
                try { Thread.sleep(350L * (attempt + 1)) } catch (_: InterruptedException) { return null }
            }
        }
        return null
    }

    fun toShareUri(context: Context, imageRef: String): Uri? {
        val uri = runCatching { Uri.parse(imageRef) }.getOrNull() ?: return null
        return when (uri.scheme?.lowercase()) {
            "content" -> uri
            "file" -> runCatching {
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", File(URI(imageRef)))
            }.getOrNull()
            "http", "https" -> persistImageUrl(context, imageRef)?.let { Uri.parse(it) }
            else -> null
        }
    }

    fun readImageBytes(context: Context, imageRef: String): ByteArray? {
        return try {
            val uri = Uri.parse(imageRef)
            when (uri.scheme?.lowercase()) {
                "content" -> context.contentResolver.openInputStream(uri)?.use { readLimitedBytes(it) }
                "file" -> readLimitedBytes(File(URI(imageRef)).inputStream())
                "http", "https" -> {
                    val request = okhttp3.Request.Builder().url(imageRef).build()
                    HttpClientFactory.client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) return null
                        response.body?.byteStream()?.use { readLimitedBytes(it) }
                    }
                }
                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }

    fun detectImageExt(url: String, bytes: ByteArray): String {
        val lower = url.lowercase()
        if (lower.contains(".jpg") || lower.contains(".jpeg")) return "jpg"
        if (lower.contains(".webp")) return "webp"
        if (lower.contains(".png")) return "png"
        if (bytes.size >= 3 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte()) return "jpg"
        if (bytes.size >= 12 && bytes[0] == 0x52.toByte() && bytes[1] == 0x49.toByte() && bytes[8] == 0x57.toByte() && bytes[9] == 0x45.toByte()) return "webp"
        return "png"
    }

    private fun saveImageRefToGeneratedFile(context: Context, imageRef: String): String? {
        val uri = runCatching { Uri.parse(imageRef) }.getOrNull() ?: return null
        return when (uri.scheme?.lowercase()) {
            "content" -> context.contentResolver.openInputStream(uri)?.use { input ->
                saveGeneratedImageStream(context, input, detectImageExt(imageRef, ByteArray(0)))
            }
            "file" -> File(URI(imageRef)).inputStream().use { input ->
                saveGeneratedImageStream(context, input, detectImageExt(imageRef, ByteArray(0)))
            }
            "http", "https" -> {
                val request = okhttp3.Request.Builder().url(imageRef).build()
                HttpClientFactory.client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return null
                    val contentType = response.header("Content-Type").orEmpty()
                    val ext = detectImageExt("$imageRef $contentType", ByteArray(0))
                    response.body?.byteStream()?.use { input -> saveGeneratedImageStream(context, input, ext) }
                }
            }
            else -> null
        }
    }

    private fun saveGeneratedImage(context: Context, bytes: ByteArray, ext: String): String {
        val dir = File(context.filesDir, GENERATED_DIR)
        dir.mkdirs()
        val file = File(dir, "gen_${System.currentTimeMillis()}_${bytes.size}.$ext")
        file.writeBytes(bytes)
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file).toString()
    }

    private fun saveGeneratedImageStream(context: Context, input: java.io.InputStream, ext: String): String? {
        val dir = File(context.filesDir, GENERATED_DIR)
        dir.mkdirs()
        val tmp = File(dir, "gen_${System.currentTimeMillis()}.tmp")
        val finalFile = File(dir, "gen_${System.currentTimeMillis()}.$ext")
        return try {
            java.io.FileOutputStream(tmp).use { output ->
                copyLimited(input, output)
            }
            if (!tmp.renameTo(finalFile)) {
                tmp.copyTo(finalFile, overwrite = true)
                tmp.delete()
            }
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", finalFile).toString()
        } catch (_: Exception) {
            tmp.delete()
            finalFile.delete()
            null
        }
    }

    private fun readLimitedBytes(input: java.io.InputStream): ByteArray {
        return input.use { source ->
            val output = java.io.ByteArrayOutputStream()
            copyLimited(source, output)
            output.toByteArray()
        }
    }

    private fun copyLimited(input: java.io.InputStream, output: java.io.OutputStream) {
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var total = 0L
        while (true) {
            val read = input.read(buffer)
            if (read == -1) break
            total += read
            if (total > MAX_IMAGE_BYTES) throw IllegalArgumentException("image exceeds $MAX_IMAGE_BYTES bytes")
            output.write(buffer, 0, read)
        }
    }
}
