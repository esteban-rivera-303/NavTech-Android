package com.zanaaustralis.feature_auth.presentation.recovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zanaaustralis.core.common.Resource
import com.zanaaustralis.core.data.remote.model.RecoveryStatusResponse
import com.zanaaustralis.core.domain.repository.AccountRecoveryRepository
import com.zanaaustralis.core.store.RecoveryStateStorage
import com.zanaaustralis.feature_auth.presentation.verification.VerificationState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author esteban.rivera01
 */
@HiltViewModel
class RecoveryVerificationFlowViewModel @Inject constructor(
    private val repository: AccountRecoveryRepository,
    private val recoveryStorage: RecoveryStateStorage
) : ViewModel() {

    private val _state = MutableStateFlow<RecoveryVerificationState>(
        RecoveryVerificationState.Loading
    )
    val state: StateFlow<RecoveryVerificationState> = _state

    private var recoveryToken: String? = null
    private var statusSnapshot: RecoveryStatusResponse? = null

    fun init() {
        recoveryToken = recoveryStorage.getToken()

        if (recoveryToken.isNullOrBlank()) {
            _state.value = RecoveryVerificationState.Error("No existe token de recuperación")
            return
        }

        refreshStatus()
    }

    fun refreshStatus() {
        val token = recoveryToken ?: return

        viewModelScope.launch {
            _state.value = RecoveryVerificationState.Loading

            when (val result = repository.getRecoveryStatus(token)) {
                is Resource.Success -> {
                    statusSnapshot = result.data
                    _state.value = resolveState(result.data)
                }

                is Resource.Error -> {
                    _state.value = RecoveryVerificationState.Error(
                        result.error ?: "No se pudo obtener el estado"
                    )
                }

                is Resource.Loading -> Unit
            }
        }
    }

    private fun resolveState(status: RecoveryStatusResponse): RecoveryVerificationState {
        return when {
            !status.emailVerified -> RecoveryVerificationState.Email
            !status.phoneVerified -> RecoveryVerificationState.Phone
            !status.kycVerified -> RecoveryVerificationState.Kyc
            else -> RecoveryVerificationState.LastStep
        }
    }

    fun onKycCompleted() {
        _state.value = RecoveryVerificationState.Completed
    }

    fun clearRecovery() {
        recoveryStorage.clear()
    }

    fun completeRecovery() = viewModelScope.launch {
        val token = recoveryStorage.getToken() ?: return@launch
        when (val result = repository.completeRecovery(token)) {
            is Resource.Success -> {
                recoveryStorage.clear()
                _state.value = RecoveryVerificationState.Completed
            }

            is Resource.Error -> _state.value =  RecoveryVerificationState.Error(
                result.error ?: "No se pudo completar la operacion"
            )
            is Resource.Loading -> {}
        }
    }
}