package com.zanaaustralis.feature_auth.presentation.verification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zanaaustralis.core.common.Resource
import com.zanaaustralis.core.data.remote.model.RequestCodeRequest
import com.zanaaustralis.core.data.remote.model.VerifyCodeRequest
import com.zanaaustralis.core.domain.usescases.AuthUseCases
import com.zanaaustralis.feature_auth.presentation.verify.VerificationType
import com.zanaaustralis.feature_auth.presentation.verify.VerifyUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author esteban.rivera01
 */

@HiltViewModel
class VerifyViewModel @Inject constructor(
    private val authUseCases: AuthUseCases,
) : ViewModel() {

    private val _uiState = MutableStateFlow(VerifyUiState())
    val uiState = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        startTimer()
    }

    fun startTimer() {
        timerJob?.cancel()

        timerJob = viewModelScope.launch {
            for (i in 30 downTo 0) {
                _uiState.update {
                    it.copy(
                        secondsRemaining = i,
                        canResend = i == 0
                    )
                }
                delay(1000)
            }
        }
    }

    fun resendCode(target: String, type: VerificationType) {
        startTimer()
        requestCode(target, type)
    }

    fun verifyCode(
        target: String,
        code: String,
        verificationType: VerificationType
    ) {
        val current = _uiState.value

        if (current.blocked) return

        _uiState.value = current.copy(isLoading = true)

        viewModelScope.launch {

            val type = if (verificationType == VerificationType.EMAIL) "email" else "phone"

            val request =
                if (verificationType == VerificationType.EMAIL)
                    VerifyCodeRequest(email = target, code = code, type = type)
                else
                    VerifyCodeRequest(phone = target, code = code, type = type)

            when ( authUseCases.verifyCode(request)) {

                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        verified = true,
                        error = null,
                        message = "Código verificado correctamente"
                    )
                }

                is Resource.Error -> {

                    val attempts = current.attempts + 1
                    val blocked = attempts >= 3

                    _uiState.value = current.copy(
                        isLoading = false,
                        attempts = attempts,
                        blocked = blocked,
                        message = null,
                        error = if (blocked)
                            "Demasiados intentos. Intenta más tarde."
                        else
                            "Código inválido (${attempts}/3)"
                    )
                }

                is Resource.Loading -> {
                    _uiState.value = current.copy(isLoading = true,message = null, error = null)
                }
            }
        }
    }

    fun requestCode(
        target: String,
        type: VerificationType
    ) {

        viewModelScope.launch {

            val request = if (type == VerificationType.EMAIL) {
                RequestCodeRequest(
                    type = "email",
                    email = target
                )
            } else {
                RequestCodeRequest(
                    type = "phone",
                    phone = target
                )
            }

            when (val result = authUseCases.requestUseCase(request)) {

                is Resource.Success -> {
                    // 🔥 reinicia timer
                    startTimer()

                    _uiState.update {
                        it.copy(
                            message = "Código reenviado correctamente"
                        )
                    }
                }

                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            message = result.error ?: "Error al reenviar código"
                        )
                    }
                }

                is Resource.Loading -> {
                    _uiState.update {
                        it.copy(isLoading = true)
                    }
                }
            }
        }
    }

    fun reset() {
        _uiState.value = VerifyUiState()
        startTimer()
    }
}