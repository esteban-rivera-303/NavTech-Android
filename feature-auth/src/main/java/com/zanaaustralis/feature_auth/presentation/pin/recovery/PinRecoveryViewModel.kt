package com.zanaaustralis.feature_auth.presentation.pin.recovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zanaaustralis.core.common.Resource
import com.zanaaustralis.feature_kyc.domain.GetValidationStatusUseCase
import com.zanaaustralis.feature_kyc.domain.StartRestorePinValidationUseCase
import com.zanaaustralis.feature_kyc.domain.VerifyRestorePinUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author esteban.rivera01
 */

@HiltViewModel
class PinRecoveryViewModel @Inject constructor(
    private val startValidation: StartRestorePinValidationUseCase,
    private val verifyValidation: VerifyRestorePinUseCase,
    private val getValidationStatusUseCase: GetValidationStatusUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PinRecoveryUiState())
    val uiState: StateFlow<PinRecoveryUiState> = _uiState

    fun startRecovery() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)

            when (val result = startValidation()) {
                is Resource.Success -> {
                    val validation = result.data.validation

                    _uiState.value = _uiState.value.copy(
                        loading = false,
                        validationId = validation.id,
                        expiresAt = validation.expires_at,
                        remainingAttempts = validation.remaining_attempts,
                        validationUrl = result.data.validation.verification_url,

                    )
                }

                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        loading = false,
                        error = result.error
                    )
                }

                Resource.Loading -> Unit
            }
        }
    }

    fun checkValidationStatus() {
        val validationId = _uiState.value.validationId ?: return

        viewModelScope.launch {
            when (val result = getValidationStatusUseCase(validationId)) {
                is Resource.Success -> {
                    val validation = result.data.validation

                    when (validation.status) {
                        "SUCCESS" -> {
                            _uiState.value = _uiState.value.copy(
                                success = true,
                                actionToken = ""//result.data.next_action_token // si aplica
                            )
                        }

                        "FAILED", "EXPIRED" -> {
                            _uiState.value = _uiState.value.copy(
                                error = "La verificación no pudo completarse. Intenta nuevamente."
                            )
                        }

                        else -> Unit // PENDING / IN_PROGRESS
                    }
                }

                is Resource.Error -> {
                    // No bloquees UX por error temporal
                }

                Resource.Loading -> Unit
            }
        }
    }



    fun verify(biometricToken: String) {
        val validationId = _uiState.value.validationId ?: return

        viewModelScope.launch {
            val result = verifyValidation(validationId, biometricToken)

            if (result.success) {
                _uiState.value = _uiState.value.copy(
                    success = true,
                    actionToken = result.next_step?.token
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    error = result.error?.message
                )
            }
        }
    }
}
