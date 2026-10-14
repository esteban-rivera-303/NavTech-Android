package com.zanaaustralis.feature_auth.presentation.userstatus

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zanaaustralis.core.common.Resource
import com.zanaaustralis.core.data.remote.model.UserStatusResponse
import com.zanaaustralis.core.domain.usescases.AuthUseCases
import com.zanaaustralis.core.secure.device.DeviceIdentityManager
import com.zanaaustralis.feature_auth.presentation.register.RegisterUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author esteban.rivera01
 */


@HiltViewModel
class UserStatusViewModel @Inject constructor(
    private val authUseCases: AuthUseCases,
    private val deviceIdentityManager: DeviceIdentityManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(UserStatusState())
    val uiState: StateFlow<UserStatusState> = _uiState

    fun checkUserStatus(rut: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, rut = rut, error = null)

            try {
                when (val result = authUseCases.userStatus(rut)) {
                    is Resource.Success -> {
                        val response = result.data
                        val newPhase = mapStatusToPhase(response)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            data = response,
                            phase = newPhase,
                            error = null
                        )
                    }

                    is Resource.Error -> _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.error ?: "Error desconocido",
                        phase = UserStatusPhase.ERROR
                    )

                    else -> Unit
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.localizedMessage ?: "Unexpected error",
                    phase = UserStatusPhase.ERROR
                )
            }
        }
    }


    fun mapStatusToPhase(response: UserStatusResponse): UserStatusPhase {
        return when {
            response.error?.isNotBlank() == true -> UserStatusPhase.ERROR
            response.status == "pending-email" -> UserStatusPhase.PENDING_EMAIL
            response.status == "pending-phone" -> UserStatusPhase.PENDING_PHONE
            response.status == "pending-kyc" -> UserStatusPhase.PENDING_KYC
            response.status == "pending-device" -> UserStatusPhase.PENDING_DEVICE
            response.status == "device-mismatch" -> UserStatusPhase.DEVICE_MISMATCH
            response.status == "device-trusted" -> UserStatusPhase.DEVICE_TRUSTED
            response.status == "ready-to-login" -> {
                deviceIdentityManager.setDeviceEnrolled(true)
                UserStatusPhase.READY_TO_LOGIN
            }

            response.status == "session-active" -> UserStatusPhase.SESSION_ACTIVE
            !response.registered -> UserStatusPhase.NOT_REGISTER
            else -> {
                UserStatusPhase.NOT_REGISTER
            }

        }
    }

    fun onEmailVerified() {
        val current = _uiState.value.data ?: return
        val updated = current.user.copy(email_verified = true).let {
            current.copy(
                user = it
            )
        }
        _uiState.value = _uiState.value.copy(data = updated)
    }

    fun onPhoneVerified() {
        val current = _uiState.value.data ?: return
        val updated = current.user.copy(phone_verified = true).let {
            current.copy(
                user = it
            )
        }
        _uiState.value = _uiState.value.copy(data = updated)
    }
}