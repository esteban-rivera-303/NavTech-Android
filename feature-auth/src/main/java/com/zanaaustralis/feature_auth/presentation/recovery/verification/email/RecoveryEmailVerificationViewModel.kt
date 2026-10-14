package com.zanaaustralis.feature_auth.presentation.recovery.verification.email

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zanaaustralis.core.common.Resource
import com.zanaaustralis.core.domain.repository.AccountRecoveryRepository
import com.zanaaustralis.core.domain.repository.AuthRepository
import com.zanaaustralis.core.domain.usescases.AuthUseCases
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
class RecoveryEmailVerificationViewModel @Inject constructor(
    private val repository: AccountRecoveryRepository,
    private val recoveryStorage: RecoveryStateStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow(VerifyUiState())
    val uiState: StateFlow<VerifyUiState> = _uiState

    private var resendTimerJob: Job? = null
    private var currentEmail: String? = null

    fun reset(email: String) {
        currentEmail = email

        _uiState.value = VerifyUiState(
            canResend = false,
            secondsRemaining = 60
        )

        startResendTimer()
    }

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

                delay(1_000)
            }

            _uiState.update {
                it.copy(
                    canResend = true,
                    secondsRemaining = 0
                )
            }
        }
    }

    fun verifyCode(code: String) {
        val token = recoveryStorage.getToken() ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            when (val result = repository.verifyEmail(token, code)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        verified = result.data.success == true
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

    fun resendCode() {
        val token = recoveryStorage.getToken() ?: return
        val type = "email"

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    message = null,
                    canResend = false
                )
            }

            val result = repository.requestNewCode(token, type)

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
                            error = result.error ?: "No se pudo reenviar el código",
                            canResend = true
                        )
                    }
                }

                else -> {
                    _uiState.update {
                        it.copy(isLoading = false)
                    }
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
@HiltViewModel
class RecoveryEmailVerificationViewModel @Inject constructor(
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

            when (val result = repository.verifyEmail(token, code)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        verified = result.data.success == true
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
}
*/

data class RecoveryEmailVerificationUiState(
    val isLoading: Boolean = false,
    val verified: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val canResend: Boolean = false,
    val secondsRemaining: Int = 60,
    val blocked: Boolean = false
)