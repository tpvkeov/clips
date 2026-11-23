package com.example.data

import android.annotation.SuppressLint
import android.content.Context
import java.net.NetworkInterface
import android.os.Build
import android.provider.Settings
import com.example.model.DeviceIdentity
import java.security.MessageDigest
import java.util.Collections
import java.util.UUID

/**
 * Collects the identifiers used for device-level security decisions.
 *
 * Android 10+ deliberately returns a random, app-local MAC address to normal apps,
 * so the MAC value below is best-effort. The fingerprint combines it with
 * Settings.Secure.ANDROID_ID, which remains the most reliable non-resettable
 * identifier available to a regular app.
 */
object DeviceIdentityCollector {

    @SuppressLint("HardwareIds")
    fun collect(context: Context): DeviceIdentity {
        val androidId = runCatching {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        }.getOrNull().takeUnless { it.isNullOrBlank() }
            ?: UUID.randomUUID().toString()

        val (mac, ip) = readNetworkIdentifiers()
        val model = listOf(Build.MANUFACTURER, Build.MODEL)
            .filter { it.isNotBlank() }
            .distinct()
            .joinToString(" ")
            .ifBlank { "Android device" }

        return DeviceIdentity(
            deviceId = androidId,
            macAddress = mac,
            ipAddress = ip,
            model = model,
            fingerprint = sha256("$androidId|$mac|$model")
        )
    }

    fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun readNetworkIdentifiers(): Pair<String, String> {
        var mac = "غير متاح"
        var ip = "غير متاح"
        runCatching {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (networkInterface in interfaces) {
                if (networkInterface.isLoopback || !networkInterface.isUp) continue

                if (mac == "غير متاح") {
                    val hardware = networkInterface.hardwareAddress
                    if (hardware != null && hardware.isNotEmpty()) {
                        mac = hardware.joinToString(":") { "%02X".format(it) }
                    }
                }

                if (ip == "غير متاح") {
                    val addresses = Collections.list(networkInterface.inetAddresses)
                    val ipv4 = addresses.firstOrNull { address ->
                        !address.isLoopbackAddress && address.hostAddress?.contains(':') == false
                    }
                    if (ipv4 != null) ip = ipv4.hostAddress ?: "غير متاح"
                }

                if (mac != "غير متاح" && ip != "غير متاح") break
            }
        }
        return mac to ip
    }
}
