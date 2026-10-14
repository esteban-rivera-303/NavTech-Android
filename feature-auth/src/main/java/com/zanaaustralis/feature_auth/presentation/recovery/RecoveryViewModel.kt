package com.zanaaustralis.feature_auth.presentation.recovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zanaaustralis.core.common.Resource
import com.zanaaustralis.core.domain.repository.AccountRecoveryRepository
import com.zanaaustralis.core.secure.device.DeviceIdentityManager
import com.zanaaustralis.core.store.RecoveryStateStorage
import com.zanaaustralis.feature_auth.presentation.recovery.legacy.RecoveryUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author esteban.rivera01
 */

@HiltViewModel
class RecoveryViewModel @Inject constructor(
    private val repository: AccountRecoveryRepository,
    private val recoveryStorage: RecoveryStateStorage,
    private val deviceIdentityManager: DeviceIdentityManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        RecoveryUiState(
            step = recoveryStorage.getStep(),
            recoveryToken = recoveryStorage.getToken()
        )
    )
    val uiState: StateFlow<RecoveryUiState> = _uiState

    fun initiate(rut: String, deviceUuid: String) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        when (val result = repository.initiateRecovery(rut, deviceUuid)) {
            is Resource.Success -> {
                val token = result.data.recovery_token
                recoveryStorage.saveState(step = 2, token = token)

                _uiState.value = _uiState.value.copy(
                    recoveryToken = token,
                    initializedSuccess = true,
                    step = 2,
                    isLoading = false
                )
            }

            is Resource.Error -> {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.error
                )
            }

            is Resource.Loading -> Unit
        }
    }
    fun getDeviceUUID() = deviceIdentityManager.getOrCreateDeviceUUID()
}

sealed class RecoveryVerificationState {
    object Loading : RecoveryVerificationState()
    object Email : RecoveryVerificationState()
    object Phone : RecoveryVerificationState()
    object Kyc : RecoveryVerificationState()
    object LastStep : RecoveryVerificationState()
    object Completed : RecoveryVerificationState()
    data class Error(val message: String) : RecoveryVerificationState()
}