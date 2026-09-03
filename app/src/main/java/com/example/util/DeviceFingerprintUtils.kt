package com.example.util

import android.content.Context
import android.os.Build
import android.provider.Settings
import java.security.MessageDigest
import java.util.Locale

object DeviceFingerprintUtils {

    private const val HARDWARE_SALT = "MARKETING_AUDIT_HW_SALT_2026_SECURE_KEY"

    /**
     * Computes a salted SHA-256 hash using secret hardware salt combined with
     * Settings.Secure.ANDROID_ID and device hardware specs (Build.MANUFACTURER, Build.MODEL, Build.HARDWARE, Build.BOARD).
     */
    fun getHardwareFingerprint(context: Context): String {
        val androidId = try {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "UNKNOWN_ANDROID_ID"
        } catch (e: Exception) {
            "UNKNOWN_ANDROID_ID"
        }

        val manufacturer = Build.MANUFACTURER ?: "UNKNOWN_MANUFACTURER"
        val model = Build.MODEL ?: "UNKNOWN_MODEL"
        val hardware = Build.HARDWARE ?: "UNKNOWN_HARDWARE"
        val board = Build.BOARD ?: "UNKNOWN_BOARD"

        val rawData = "$HARDWARE_SALT|$androidId|$manufacturer|$model|$hardware|$board"

        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(rawData.toByteArray(Charsets.UTF_8))
            hashBytes.joinToString("") { "%02X".format(it) }
        } catch (e: Exception) {
            "DEFAULT_FALLBACK_FINGERPRINT_HASH"
        }
    }

    /**
     * Formats fingerprint into a short, clean representation (e.g., SHA256:8F3A-42C1).
     */
    fun getFormattedFingerprint(fingerprint: String): String {
        val clean = fingerprint.replace("-", "").uppercase(Locale.ROOT)
        return if (clean.length >= 8) {
            val part1 = clean.substring(0, 4)
            val part2 = clean.substring(4, 8)
            "SHA256:$part1-$part2"
        } else {
            "SHA256:${clean.take(8)}"
        }
    }

    /**
     * Extracts clean model name for display (e.g., Google Pixel 8, Samsung Galaxy S23, etc.).
     */
    fun getHardwareModelName(): String {
        val manufacturer = Build.MANUFACTURER?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() } ?: "Generic"
        val model = Build.MODEL ?: "Device"
        return if (model.startsWith(manufacturer, ignoreCase = true)) {
            model
        } else {
            "$manufacturer $model"
        }
    }
}
