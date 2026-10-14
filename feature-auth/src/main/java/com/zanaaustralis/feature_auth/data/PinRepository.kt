package com.zanaaustralis.feature_auth.data

/**
 * @author esteban.rivera01
 */
interface PinRepository {
    suspend fun hasPin(): Boolean
    suspend fun savePin(pin: String)
    suspend fun verifyPin(pin: String): PinVerificationResult
    suspend fun remainingAttempts(): Int
    suspend fun clearPin()
}

sealed interface PinVerificationResult {
    data object Valid : PinVerificationResult
    data class Invalid(val remainingAttempts: Int) : PinVerificationResult
    data object Locked : PinVerificationResult
}
