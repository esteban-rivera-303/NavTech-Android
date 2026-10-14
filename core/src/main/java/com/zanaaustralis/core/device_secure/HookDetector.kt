package com.zanaaustralis.core.device_secure

import javax.inject.Inject

/**
 * @author esteban.rivera01
 */

class HookDetector @Inject constructor() {

    fun isHooked(): Boolean {
        return detectFrida() || detectXposed()
    }

    private fun detectFrida(): Boolean {
        return try {
            Runtime.getRuntime().exec("ps").inputStream.bufferedReader().readText()
                .contains("frida")
        } catch (e: Exception) {
            false
        }
    }

    private fun detectXposed(): Boolean {
        return try {
            ClassLoader.getSystemClassLoader()
                .loadClass("de.robv.android.xposed.XposedBridge")
            true
        } catch (e: Exception) {
            false
        }
    }
}