package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.util.DeviceFingerprintUtils

data class DeviceBindingInfo(
    val isBound: Boolean,
    val boundFingerprint: String? = null,
    val boundDeviceModel: String? = null,
    val boundFormattedFingerprint: String? = null
)

sealed class LoginResult {
    object Success : LoginResult()
    object InvalidCredentials : LoginResult()
    data class DeviceMismatch(
        val boundDeviceModel: String,
        val boundFingerprint: String,
        val currentFingerprint: String
    ) : LoginResult()
}

class DeviceBindingManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("device_session_binding_prefs", Context.MODE_PRIVATE)

    fun getBindingInfo(username: String): DeviceBindingInfo {
        val boundFp = prefs.getString("bound_fp_$username", null)
        val boundModel = prefs.getString("bound_model_$username", null)
        return if (!boundFp.isNullOrEmpty()) {
            DeviceBindingInfo(
                isBound = true,
                boundFingerprint = boundFp,
                boundDeviceModel = boundModel ?: "Bound Hardware",
                boundFormattedFingerprint = DeviceFingerprintUtils.getFormattedFingerprint(boundFp)
            )
        } else {
            DeviceBindingInfo(
                isBound = false,
                boundFingerprint = null,
                boundDeviceModel = null,
                boundFormattedFingerprint = null
            )
        }
    }

    fun verifyAndBindLogin(username: String, overrideCurrentFingerprint: String? = null): LoginResult {
        val currentFp = overrideCurrentFingerprint ?: DeviceFingerprintUtils.getHardwareFingerprint(context)
        val currentModel = DeviceFingerprintUtils.getHardwareModelName()

        val boundInfo = getBindingInfo(username)

        return if (!boundInfo.isBound) {
            // First Login Binding: Bind account directly to initial device hardware fingerprint
            prefs.edit()
                .putString("bound_fp_$username", currentFp)
                .putString("bound_model_$username", currentModel)
                .putLong("bound_time_$username", System.currentTimeMillis())
                .apply()
            LoginResult.Success
        } else {
            // Session Verification on Login: Compare current fingerprint against registered bound fingerprint
            if (boundInfo.boundFingerprint == currentFp) {
                LoginResult.Success
            } else {
                // Anti-Circumvention Enforcement: Block authentication
                LoginResult.DeviceMismatch(
                    boundDeviceModel = boundInfo.boundDeviceModel ?: "Registered Device",
                    boundFingerprint = boundInfo.boundFormattedFingerprint ?: "SHA256:????-????",
                    currentFingerprint = DeviceFingerprintUtils.getFormattedFingerprint(currentFp)
                )
            }
        }
    }

    fun resetBinding(username: String) {
        prefs.edit()
            .remove("bound_fp_$username")
            .remove("bound_model_$username")
            .remove("bound_time_$username")
            .apply()
    }

    fun simulateMismatchBinding(username: String) {
        val simulatedFp = "SIMULATED_UNAUTHORIZED_OTHER_HARDWARE_FINGERPRINT_9999"
        val simulatedModel = "External Device (Samsung Galaxy Ultra)"
        prefs.edit()
            .putString("bound_fp_$username", simulatedFp)
            .putString("bound_model_$username", simulatedModel)
            .putLong("bound_time_$username", System.currentTimeMillis())
            .apply()
    }
}
