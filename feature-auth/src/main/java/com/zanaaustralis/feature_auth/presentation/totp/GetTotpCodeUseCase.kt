package com.zanaaustralis.feature_auth.presentation.totp

import com.zanaaustralis.feature_auth.data.local.TotpManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * @author esteban.rivera01
 */

class ObserveTotpUseCase @Inject constructor(
    private val totpManager: TotpManager
) {

    operator fun invoke(): Flow<TotpState> = flow {
        while (true) {

            val code = totpManager.getCurrentCode()
            val remaining = getSecondsRemaining()

            emit(
                TotpState(
                    code = code ?: "",
                    secondsRemaining = remaining
                )
            )

            delay(1000)
        }
    }
}

data class TotpState(
    val code: String,
    val secondsRemaining: Long
)

fun getSecondsRemaining(): Long {
    val now = System.currentTimeMillis() / 1000
    return 30 - (now % 30)
}