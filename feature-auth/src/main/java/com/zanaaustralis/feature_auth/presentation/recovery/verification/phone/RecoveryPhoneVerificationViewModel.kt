package com.zanaaustralis.feature_auth.presentation.recovery.verification.phone

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zanaaustralis.core.common.Resource
import com.zanaaustralis.core.domain.repository.AccountRecoveryRepository
import com.zanaaustralis.core.store.RecoveryStateStorage
import com.zanaaustralis.feature_auth.presentation.verify.VerifyUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author esteban.rivera01
 */

@HiltViewModel
class RecoveryPhoneVerificationViewModel @Inject constructor(
    private val repository: AccountRecoveryRepository,
    private val recoveryStorage: RecoveryStateStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow(VerifyUiState())
    val uiState: StateFlow<VerifyUiState> = _uiState

    private var resendTimerJob: Job? = null
    private var currentPhone: String? = null

    // 🔥 RESET
    fun reset(phone: String) {
        currentPhone = phone

        _uiState.value = VerifyUiState(
            canResend = false,
            secondsRemaining = 60
        )

        startResendTimer()
    }

    // 🔥 TIMER
    private fun startResendTimer() {
        resendTimerJob?.cancel()

        resendTimerJob = viewModelScope.launch {
            for (seconds in 60 downTo 1) {
                _uiState.update {
                    it.copy(
                        canResend = false,
                        secondsRemaining = seconds
                    )
                }
                delay(1000)
            }

            _uiState.update {
                it.copy(
                    canResend = true,
                    secondsRemaining = 0
                )
            }
        }
    }

    // 🔥 VERIFY
    fun verifyCode(code: String) {
        val token = recoveryStorage.getToken() ?: return

        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, error = null)
            }

            when (val result = repository.verifyPhone(token, code)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            verified = result.data?.success == true
                        )
                    }
                }

                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.error
                        )
                    }
                }

                else -> {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    // 🔥 RESEND (CLAVE)
    fun resendCode(phone: String = currentPhone.orEmpty()) {
        val token = recoveryStorage.getToken() ?: return
        val type = "phone"

        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    message = null,
                    canResend = false
                )
            }

            val result = repository.requestNewCode(token,type)

            when (result) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            message = "Código reenviado correctamente"
                        )
                    }

                    startResendTimer()
                }

                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.error ?: "Error al reenviar",
                            canResend = true
                        )
                    }
                }

                else -> {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    override fun onCleared() {
        resendTimerJob?.cancel()
        super.onCleared()
    }
}
/*
data class RecoveryPhoneVerificationUiState(
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null
)
*/

/*
@HiltViewModel
class RecoveryPhoneVerificationViewModel @Inject constructor(
    private val repository: AccountRecoveryRepository,
    private val recoveryStorage: RecoveryStateStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow(VerifyUiState())
    val uiState: StateFlow<VerifyUiState> = _uiState

    fun reset() {
        _uiState.value = VerifyUiState()
    }

    fun verifyCode(code: String) {
        val token = recoveryStorage.getToken() ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            when (val result = repository.verifyPhone(token, code)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        verified = result.data?.success == true
                    )
                }

                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.error
                    )
                }

                else -> Unit
            }
        }
    }
}*/
