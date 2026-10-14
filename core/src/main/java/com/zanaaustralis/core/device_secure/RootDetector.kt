package com.zanaaustralis.core.device_secure

import android.content.Context
import java.io.File
import javax.inject.Inject

/**
 * @author esteban.rivera01
 */

class RootDetector @Inject constructor(private val context: Context){
    fun isDeviceRooted(): Boolean {
        return checkSuBinary() ||
                checkRootApps() ||
                checkDangerousProps() ||
                checkRWPaths() ||
                canExecuteSu()
    }

    // 1. Buscar binario su
    private fun checkSuBinary(): Boolean {
        val paths = arrayOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/data/local/su"
        )

        return paths.any { File(it).exists() }
    }

    // 2. Apps conocidas de root
    private fun checkRootApps(): Boolean {
        val packages = listOf(
            "com.topjohnwu.magisk",
            "eu.chainfire.supersu",
            "com.koushikdutta.superuser",
            "com.noshufou.android.su"
        )

        return packages.any { isPackageInstalled(it) }
    }

    private fun isPackageInstalled(packageName: String): Boolean {
        return try {
            val pm = context.packageManager
            pm.getPackageInfo(packageName, 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    // 3. Props peligrosas
    private fun checkDangerousProps(): Boolean {
        val props = mapOf(
            "ro.debuggable" to "1",
            "ro.secure" to "0"
        )

        val process = Runtime.getRuntime().exec("getprop")
        val input = process.inputStream.bufferedReader().readText()

        return props.any { (key, value) ->
            input.contains("$key=$value")
        }
    }

    // 4. Paths con permisos sospechosos
    private fun checkRWPaths(): Boolean {
        val paths = listOf(
            "/system",
            "/system/bin",
            "/system/sbin",
            "/system/xbin"
        )

        return paths.any {
            try {
                File(it).canWrite()
            } catch (e: Exception) {
                false
            }
        }
    }

    // 5. Intentar ejecutar su
    private fun canExecuteSu(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
            val output = process.inputStream.bufferedReader().readText()
            output.contains("uid=0")
        } catch (e: Exception) {
            false
        }
    }
}