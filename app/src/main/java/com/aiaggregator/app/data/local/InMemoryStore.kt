package com.aiaggregator.app.data.local

import android.content.Context
import android.util.Log
import com.aiaggregator.app.data.model.Message
import com.aiaggregator.app.data.model.Session
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object InMemoryStore {

    private val _sessions = MutableStateFlow<List<Session>>(emptyList())
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    private var ctx: Context? = null
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val fileMutex = Mutex()
    private val storeScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    @Volatile private var lastMessagesSaveMs: Long = 0L
    private var pendingMessagesSaveJob: Job? = null

    fun initialize(context: Context) {
        if (ctx != null) return
        ctx = context.applicationContext
        _sessions.value = loadFromFileSync("sessions.json") ?: emptyList()
        _messages.value = loadFromFileSync("messages.json") ?: emptyList()
        Log.d("InMemoryStore", "Loaded ${_sessions.value.size} sessions, ${_messages.value.size} messages")
    }

    // === Session ===

    suspend fun insertSession(session: Session) {
        _sessions.update { list ->
            val idx = list.indexOfFirst { it.id == session.id }
            if (idx >= 0) { list.toMutableList().also { it[idx] = session } } else { list + session }
        }
        storeScope.launch { saveSessionsOnIo() }
    }

    fun getAllSessions(): Flow<List<Session>> = _sessions.map { list ->
        list.sortedByDescending { it.lastActiveAt }
    }

    suspend fun getSession(sessionId: String): Session? =
        _sessions.value.find { it.id == sessionId }

    suspend fun deleteSession(sessionId: String) {
        _sessions.update { list -> list.filter { it.id != sessionId } }
        _messages.update { list -> list.filter { it.sessionId != sessionId } }
        saveAllOnIo()
    }

    suspend fun updateSessionTitle(sessionId: String, title: String) {
        _sessions.update { list -> list.map { if (it.id == sessionId) it.copy(title = title) else it } }
        storeScope.launch { saveSessionsOnIo() }
    }

    suspend fun updateSessionActivity(sessionId: String, timestamp: Long) {
        _sessions.update { list ->
            list.map { if (it.id == sessionId) it.copy(lastActiveAt = timestamp, messageCount = it.messageCount + 1) else it }
        }
        storeScope.launch { saveSessionsOnIo() }
    }

    // === Message ===

    fun insertMessage(message: Message, saveImmediately: Boolean = false) {
        _messages.update { list ->
            val mutable = list.toMutableList()
            val idx = mutable.indexOfFirst { it.id == message.id }
            if (idx >= 0) mutable[idx] = message else mutable.add(message)
            mutable
        }
        if (saveImmediately) {
            pendingMessagesSaveJob?.cancel()
            pendingMessagesSaveJob = storeScope.launch {
                lastMessagesSaveMs = System.currentTimeMillis()
                saveMessagesOnIo()
            }
            return
        }
        // Debounced save: at most once per 500ms to avoid thrashing during streaming
        val now = System.currentTimeMillis()
        if (now - lastMessagesSaveMs > 500) {
            lastMessagesSaveMs = now
            pendingMessagesSaveJob?.cancel()
            pendingMessagesSaveJob = storeScope.launch {
                saveMessagesOnIo()
            }
        } else {
            pendingMessagesSaveJob?.cancel()
            pendingMessagesSaveJob = storeScope.launch {
                delay(500)
                lastMessagesSaveMs = System.currentTimeMillis()
                saveMessagesOnIo()
            }
        }
    }

    fun deleteMessage(messageId: String, saveImmediately: Boolean = true) {
        _messages.update { list -> list.filter { it.id != messageId } }
        if (saveImmediately) {
            pendingMessagesSaveJob?.cancel()
            pendingMessagesSaveJob = storeScope.launch {
                lastMessagesSaveMs = System.currentTimeMillis()
                saveMessagesOnIo()
            }
        } else {
            insertMessageSaveOnlyDebounced()
        }
    }

    private fun insertMessageSaveOnlyDebounced() {
        val now = System.currentTimeMillis()
        pendingMessagesSaveJob?.cancel()
        pendingMessagesSaveJob = storeScope.launch {
            val waitMs = (500 - (now - lastMessagesSaveMs)).coerceAtLeast(0)
            if (waitMs > 0) delay(waitMs)
            lastMessagesSaveMs = System.currentTimeMillis()
            saveMessagesOnIo()
        }
    }

    fun getMessagesBySession(sessionId: String): Flow<List<Message>> =
        _messages.map { list -> list.filter { it.sessionId == sessionId }.sortedBy { it.timestamp } }

    suspend fun deleteMessagesBySession(sessionId: String) {
        _messages.update { list -> list.filter { it.sessionId != sessionId } }
        saveMessagesOnIo()
    }

    // === Bulk ===

    suspend fun clearAll() {
        _sessions.value = emptyList()
        _messages.value = emptyList()
        saveAllOnIo()
    }

    suspend fun clearOlderThan(timestamp: Long): Int {
        val filteredMsgs = _messages.value.filter { it.timestamp >= timestamp }
        val before = _messages.value.size + _sessions.value.size
        val activeSessionIds = filteredMsgs.map { it.sessionId }.toSet()
        _messages.value = filteredMsgs
        _sessions.value = _sessions.value.filter { it.id in activeSessionIds }
        saveAllOnIo()
        return before - (_messages.value.size + _sessions.value.size)
    }

    suspend fun clearNewerThan(timestamp: Long): Int {
        val toDelete = _sessions.value.filter { it.createdAt >= timestamp }
        val ids = toDelete.map { it.id }.toSet()
        _sessions.update { list -> list.filter { it.id !in ids } }
        _messages.update { list -> list.filter { it.sessionId !in ids } }
        saveAllOnIo()
        return toDelete.size
    }

    fun countNewerThan(timestamp: Long): Int =
        _sessions.value.count { it.createdAt >= timestamp }

    fun countOlderThan(timestamp: Long): Int {
        val oldMsgs = _messages.value.filter { it.timestamp < timestamp }
        val oldIds = oldMsgs.map { it.sessionId }.toSet()
        val orphans = _sessions.value.filter { s ->
            s.id in oldIds && _messages.value.none { m -> m.sessionId == s.id && m.timestamp >= timestamp }
        }
        return oldMsgs.size + orphans.size
    }

    /**
     * Flush pending message saves immediately. Call before app goes to background.
     */
    suspend fun flushMessages() {
        pendingMessagesSaveJob?.cancel()
        pendingMessagesSaveJob = null
        lastMessagesSaveMs = System.currentTimeMillis()
        saveMessagesOnIo()
    }

    // === File I/O ===

    private suspend fun saveSessionsOnIo() = withContext(Dispatchers.IO) {
        fileMutex.withLock { saveSessionsSync() }
    }

    private suspend fun saveMessagesOnIo() = withContext(Dispatchers.IO) {
        fileMutex.withLock { saveMessagesSync() }
    }

    private suspend fun saveAllOnIo() = withContext(Dispatchers.IO) {
        fileMutex.withLock {
            saveSessionsSync()
            saveMessagesSync()
        }
    }

    private fun saveSessionsSync() {
        ctx?.let { c ->
            try {
                val file = c.getFileStreamPath("sessions.json")
                val tmp = java.io.File(file.parent, "sessions.json.tmp")
                file.parentFile?.mkdirs()
                java.io.FileOutputStream(tmp).use { fos ->
                    fos.write(json.encodeToString(_sessions.value).toByteArray())
                    fos.fd.sync()
                }
                tmp.renameTo(file)
            } catch (e: Exception) { Log.e("InMemoryStore", "saveSessions failed", e) }
        }
    }

    private fun saveMessagesSync() {
        ctx?.let { c ->
            try {
                val file = c.getFileStreamPath("messages.json")
                val tmp = java.io.File(file.parent, "messages.json.tmp")
                file.parentFile?.mkdirs()
                java.io.FileOutputStream(tmp).use { fos ->
                    fos.write(json.encodeToString(_messages.value).toByteArray())
                    fos.fd.sync()
                }
                tmp.renameTo(file)
            } catch (e: Exception) { Log.e("InMemoryStore", "saveMessages failed", e) }
        }
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
