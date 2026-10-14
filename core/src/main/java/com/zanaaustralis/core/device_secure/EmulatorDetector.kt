package com.zanaaustralis.core.device_secure

import android.os.Build
import javax.inject.Inject

/**
 * @author esteban.rivera01
 */

class EmulatorDetector @Inject constructor() {
    fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.contains("generic")
                || Build.MODEL.contains("Emulator")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.BRAND.startsWith("generic")
                || Build.DEVICE.startsWith("generic"))
    }
}