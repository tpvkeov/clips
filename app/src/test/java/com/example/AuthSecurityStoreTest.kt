package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.AuthSecurityStore
import com.example.model.DeviceIdentity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AuthSecurityStoreTest {

    @Test
    fun `every ten failed attempts locks sign in for thirty seconds`() {
        val store = AuthSecurityStore(ApplicationProvider.getApplicationContext<Context>())
        repeat(9) { index ->
            val result = store.recordFailedAttempt(startTime + index)
            assertFalse(result.locked)
            assertEquals(index + 1, result.failedAttempts)
        }

        val tenth = store.recordFailedAttempt(startTime + 9)
        assertTrue(tenth.locked)
        assertEquals(0, tenth.failedAttempts)
        assertTrue(store.isLocked(startTime + 9))
        assertFalse(store.isLocked(startTime + 9 + LOCK_DURATION_MS))

        store.clearFailedAttempts()
        assertFalse(store.isLocked(startTime + 9))
        assertEquals(0, store.failedAttempts())
    }

    @Test
    fun `banned device fingerprint is remembered`() {
        val store = AuthSecurityStore(ApplicationProvider.getApplicationContext<Context>())
        val fingerprint = "test_fingerprint_${System.nanoTime()}"
        assertFalse(store.isDeviceBanned(fingerprint))
        store.banDevice(fingerprint)
        assertTrue(store.isDeviceBanned(fingerprint))
        store.unbanDevice(fingerprint)
        assertFalse(store.isDeviceBanned(fingerprint))
    }

    @Test
    fun `device ban matches stable and extended fingerprints`() {
        val store = AuthSecurityStore(ApplicationProvider.getApplicationContext<Context>())
        val unique = System.nanoTime()
        val device = DeviceIdentity(
            deviceId = "device_$unique",
            macAddress = "mac_$unique",
            ipAddress = "192.0.2.1",
            model = "Test Model",
            fingerprint = "stable_$unique",
            extendedFingerprint = "extended_$unique"
        )

        assertFalse(store.isDeviceBanned(device))
        store.banDevice(device)
        assertTrue(store.isDeviceBanned(device))
        assertTrue(store.isDeviceBanned(device.fingerprint))
        assertTrue(store.isDeviceBanned(device.extendedFingerprint))
        store.unbanDevice(device)
        assertFalse(store.isDeviceBanned(device))
    }

    private companion object {
        const val startTime = 1_000_000L
        const val LOCK_DURATION_MS = 30_000L
    }
}
