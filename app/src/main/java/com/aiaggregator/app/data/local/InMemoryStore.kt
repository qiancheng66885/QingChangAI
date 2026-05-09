package com.aiaggregator.app.data.local

import android.content.Context
import android.util.Log
import com.aiaggregator.app.data.model.Message
import com.aiaggregator.app.data.model.Session
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * 混合存储 — MutableStateFlow 即时 UI + JSON 文件磁盘持久化。
 * 避免 Room 注解处理器兼容性问题，直接用 kotlinx.serialization 序列化到文件。
 */
object InMemoryStore {

    private val _sessions = MutableStateFlow<List<Session>>(emptyList())
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    private var ctx: Context? = null
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    fun initialize(context: Context) {
        if (ctx != null) return
        ctx = context.applicationContext
        _sessions.value = loadFromFileSync("sessions.json") ?: emptyList()
        _messages.value = loadFromFileSync("messages.json") ?: emptyList()
        Log.d("InMemoryStore", "Loaded ${_sessions.value.size} sessions, ${_messages.value.size} messages")
    }

    // === Session ===

    suspend fun insertSession(session: Session) {
        _sessions.value = _sessions.value + session
        saveSessionsSync()
    }

    fun getAllSessions(): Flow<List<Session>> = _sessions

    suspend fun getSession(sessionId: String): Session? =
        _sessions.value.find { it.id == sessionId }

    suspend fun deleteSession(sessionId: String) {
        _sessions.value = _sessions.value.filter { it.id != sessionId }
        _messages.value = _messages.value.filter { it.sessionId != sessionId }
        saveSessionsSync()
        saveMessagesSync()
    }

    suspend fun updateSessionTitle(sessionId: String, title: String) {
        _sessions.value = _sessions.value.map { if (it.id == sessionId) it.copy(title = title) else it }
        saveSessionsSync()
    }

    suspend fun updateSessionActivity(sessionId: String, timestamp: Long) {
        _sessions.value = _sessions.value.map {
            if (it.id == sessionId) it.copy(lastActiveAt = timestamp, messageCount = it.messageCount + 1) else it
        }
        saveSessionsSync()
    }

    // === Message ===

    fun insertMessage(message: Message) {
        val list = _messages.value.toMutableList()
        val idx = list.indexOfFirst { it.id == message.id }
        if (idx >= 0) list[idx] = message else list.add(message)
        _messages.value = list
        // Persist on background thread — StateFlow update is instant
        Thread { saveMessagesSync() }.start()
    }

    fun getMessagesBySession(sessionId: String): Flow<List<Message>> =
        _messages.map { list -> list.filter { it.sessionId == sessionId }.sortedBy { it.timestamp } }

    suspend fun deleteMessagesBySession(sessionId: String) {
        _messages.value = _messages.value.filter { it.sessionId != sessionId }
        saveMessagesSync()
    }

    // === Bulk ===

    fun clearAll() {
        _sessions.value = emptyList()
        _messages.value = emptyList()
        saveSessionsSync(); saveMessagesSync()
    }

    fun clearOlderThan(timestamp: Long): Int {
        val before = _messages.value.size + _sessions.value.size
        _messages.value = _messages.value.filter { it.timestamp >= timestamp }
        _sessions.value = _sessions.value.filter { s -> _messages.value.any { it.sessionId == s.id } }
        saveSessionsSync(); saveMessagesSync()
        return before - (_messages.value.size + _sessions.value.size)
    }

    fun clearNewerThan(timestamp: Long): Int {
        val toDelete = _sessions.value.filter { it.createdAt >= timestamp }
        val ids = toDelete.map { it.id }.toSet()
        _sessions.value = _sessions.value.filter { it.id !in ids }
        _messages.value = _messages.value.filter { it.sessionId !in ids }
        saveSessionsSync(); saveMessagesSync()
        return toDelete.size
    }

    fun countNewerThan(timestamp: Long): Int {
        return _sessions.value.count { it.createdAt >= timestamp }
    }

    fun countOlderThan(timestamp: Long): Int {
        val oldMsgs = _messages.value.filter { it.timestamp < timestamp }
        val oldIds = oldMsgs.map { it.sessionId }.toSet()
        val orphans = _sessions.value.filter { s ->
            s.id in oldIds && _messages.value.none { m -> m.sessionId == s.id && m.timestamp >= timestamp }
        }
        return oldMsgs.size + orphans.size
    }

    // === File I/O (synchronous — data is small, IO is fast) ===

    private fun saveSessionsSync() {
        ctx?.let { c ->
            try {
                val file = c.getFileStreamPath("sessions.json")
                file.parentFile?.mkdirs()
                java.io.FileOutputStream(file).use { fos ->
                    fos.write(json.encodeToString(_sessions.value).toByteArray())
                    fos.fd.sync() // force to disk
                }
            } catch (e: Exception) { Log.e("InMemoryStore", "saveSessions failed", e) }
        }
    }

    private fun saveMessagesSync() {
        ctx?.let { c ->
            try {
                val data = json.encodeToString(_messages.value)
                val file = c.getFileStreamPath("messages.json")
                file.parentFile?.mkdirs()
                java.io.FileOutputStream(file).use { fos ->
                    fos.write(data.toByteArray())
                    fos.fd.sync()
                }
                Log.d("InMemoryStore", "Saved ${_messages.value.size} msgs (${data.length} bytes)")
            } catch (e: Exception) { Log.e("InMemoryStore", "saveMessages failed", e) }
        } ?: Log.e("InMemoryStore", "saveMessages: ctx is null!")
    }

    private inline fun <reified T> loadFromFileSync(name: String): List<T>? {
        return try {
            val file = ctx?.getFileStreamPath(name)
            if (file != null && file.exists()) {
                json.decodeFromString<List<T>>(file.readText())
            } else null
        } catch (e: Exception) { Log.e("InMemoryStore", "loadSync $name failed", e); null }
    }

}
