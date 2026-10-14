package com.zanaaustralis.feature_auth.data.local

import android.util.Log
import com.zanaaustralis.core.data.local.TotpStorage
import javax.inject.Inject

/**
 * @author esteban.rivera01
 */

class TotpManager @Inject constructor(
    private val storage: TotpStorage,
    private val generator: TotpGenerator
) {

    fun getCurrentCode(): String? {
        val secret = storage.getSecret() ?: return null
        return generator.generate(secret)
    }

    fun getSecondsRemaining(): Long {
        val now = System.currentTimeMillis() / 1000
        return 30 - (now % 30)
    }
}