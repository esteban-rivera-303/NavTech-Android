package com.zanaaustralis.feature_auth.presentation.verification

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zanaaustralis.core.common.Resource
import com.zanaaustralis.core.data.remote.model.UserStatusResponse
import com.zanaaustralis.core.domain.model.User
import com.zanaaustralis.core.domain.usescases.AuthUseCases
import com.zanaaustralis.feature_auth.presentation.userstatus.UserStatusPhase
import com.zanaaustralis.feature_auth.presentation.userstatus.UserStatusState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author esteban.rivera01
 */
@HiltViewModel
class VerificationFlowViewModel @Inject constructor(
    private val authUseCases: AuthUseCases
) : ViewModel() {

    private val _state = MutableStateFlow<VerificationState>(VerificationState.Loading)
    val state: StateFlow<VerificationState> = _state

    private var response: UserStatusResponse? = null

    fun init(rut: String) {
        checkUserStatus(rut)
    }

    private fun checkUserStatus(rut: String) {
        viewModelScope.launch {
            _state.value = VerificationState.Loading

            try {
                when (val result = authUseCases.userStatus(rut)) {

                    is Resource.Success -> {
                        val data = result.data
                        response = data
                        _state.value = resolveInitialState(data)
                    }

                    is Resource.Error -> {
                        _state.value = VerificationState.Error(
                            result.error ?: "Error desconocido"
                        )
                    }

                    else -> Unit
                }

            } catch (e: Exception) {
                _state.value = VerificationState.Error(
                    e.localizedMessage ?: "Unexpected error"
                )
            }
        }
    }

    private suspend fun refreshStatus() {
        val rut = response?.user?.rut ?: return

        when (val result = authUseCases.userStatus(rut)) {

            is Resource.Success -> {
                val data = result.data
                response = data
                _state.value = resolveInitialState(data)
            }

            is Resource.Error -> {
                _state.value = VerificationState.Error(
                    result.error ?: "Error refrescando estado"
                )
            }

            else -> Unit
        }
    }

    private fun resolveInitialState(response: UserStatusResponse): VerificationState {
        val completed = response.completed_steps.toSet()

        return when {
            !completed.contains("email_verified") -> VerificationState.Email
            !completed.contains("phone_verified") -> VerificationState.Phone
            response.user.kyc_status != "verified" -> VerificationState.Kyc
            else -> VerificationState.Completed
        }
    }

    fun onEmailVerified() {
        viewModelScope.launch {
            refreshStatus()
        }
    }

    fun onPhoneVerified() {
        viewModelScope.launch {
            refreshStatus()
        }
    }

    fun onKycCompleted() {
        _state.value = VerificationState.Completed
    }
}
sealed class VerificationState {
    object Loading : VerificationState()
    object Email : VerificationState()
    object Phone : VerificationState()
    object Kyc : VerificationState()
    object Completed : VerificationState()
    data class Error(val message: String) : VerificationState()
}
