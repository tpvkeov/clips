package com.example.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Shares one in-flight network result between every caller asking for the same
 * key. Configuration changes and parallel composables often request the same
 * feed/ticket at the same time; without this they would each spend a Supabase
 * request. Results are never cached here, only merged while running.
 */
object SupabaseRequestCoalescer {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val mutex = Mutex()
    private val inFlight = mutableMapOf<String, Deferred<Any?>>()

    suspend fun <T> once(key: String, block: suspend () -> T): T {
        val deferred: Deferred<Any?> = mutex.withLock {
            inFlight[key] ?: scope.async {
                block() as Any?
            }.also { inFlight[key] = it }
        }
        return try {
            @Suppress("UNCHECKED_CAST")
            deferred.await() as T
        } finally {
            mutex.withLock {
                if (inFlight[key] === deferred) inFlight.remove(key)
            }
        }
    }

    fun clear() {
        inFlight.clear()
    }
}
