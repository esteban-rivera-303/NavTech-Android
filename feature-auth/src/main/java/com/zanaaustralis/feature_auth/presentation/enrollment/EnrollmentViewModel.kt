package com.zanaaustralis.feature_auth.presentation.enrollment

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zanaaustralis.core.common.Resource
import com.zanaaustralis.core.domain.model.OnboardingState
import com.zanaaustralis.core.domain.usescases.AuthUseCases
import com.zanaaustralis.core.domain.usescases.recovery.ReactivateTrustedDeviceUseCase
import com.zanaaustralis.core.domain.usescases.recovery.RecoveryEnrollDeviceUsesCases
import com.zanaaustralis.core.secure.device.DeviceIdentityManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author esteban.rivera01
 */

@HiltViewModel
class EnrollmentViewModel @Inject constructor(
    private val authUseCases: AuthUseCases,
    private val deviceIdentityManager: DeviceIdentityManager,
    private val reactivateTrustedDeviceUseCase: ReactivateTrustedDeviceUseCase,
    private val recoveryEnrollDeviceUsesCases: RecoveryEnrollDeviceUsesCases,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EnrollmentUiState())
    val uiState: StateFlow<EnrollmentUiState> = _uiState

    fun enrollDevice(state: OnboardingState, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                _uiState.value = EnrollmentUiState(isLoading = true)

                when (val result = authUseCases.onboarding.invoke(state.rut ?: "")) {
                    is Resource.Success -> {

                        deviceIdentityManager.setDeviceEnrolled(true)
                        val resultLogin = authUseCases.loginUserUseCase.invoke()
                        if (resultLogin is Resource.Success) {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                success = true,
                                message = "Enrolamiento exitoso",
                                enrolledUser = null
                            )
                        }
                        else{
                            onResult(false, "No se pudo crear la sesión")
                        }
                    }

                    is Resource.Error -> {
                        if (result.error == "User already has a device enrolled") {
                            deviceIdentityManager.setDeviceEnrolled(true)
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                success = true,
                                message = "Enrolamiento exitoso",
                                enrolledUser = null
                            )
                        } else {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                success = false,
                                message = result.error
                            )
                            deviceIdentityManager.clear()
                        }

                    }

                    is Resource.Loading -> _uiState.value = EnrollmentUiState(isLoading = true)

                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(success = false, message = e.localizedMessage)
                onResult(false, e.localizedMessage ?: "Error desconocido")
            }
        }
    }

    fun reactivateDevice(rut: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _uiState.value = EnrollmentUiState(isLoading = true)

            when (reactivateTrustedDeviceUseCase.invoke(rut)) {

                is Resource.Success -> {
                    onResult(true, rut)
                }

                is Resource.Error -> {
                    _uiState.value = EnrollmentUiState(isLoading = false, message = "Error al crear la sesión.", success =  false)
                }

                Resource.Loading -> {
                    _uiState.value = EnrollmentUiState(isLoading = true)
                }
            }
        }
    }

    fun recoveryEnrollDevice(rut: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _uiState.value = EnrollmentUiState(isLoading = true)

            when (recoveryEnrollDeviceUsesCases.invoke(rut)) {

                is Resource.Success -> {
                    onResult(true, rut)
                }

                is Resource.Error -> {
                    _uiState.value = EnrollmentUiState(isLoading = false)
                }

                Resource.Loading -> {
                    _uiState.value = EnrollmentUiState(isLoading = true)
                }
            }
        }
    }

    fun getDeviceInfo() = deviceIdentityManager.getDeviceInfo()

}