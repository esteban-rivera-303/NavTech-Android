package com.zanaaustralis.feature_auth.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zanaaustralis.core.domain.repository.AuthRepository
import com.zanaaustralis.core.session.SessionManagerImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author esteban.rivera01
 */

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManagerImpl: SessionManagerImpl
) : ViewModel() {


    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun login(device_uuid: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState(isLoading = true)


           /* val response = authRepository.login(LoginRequest(device_uuid = device_uuid))
            _uiState.value = LoginUiState(isLoading = false)*/

        }
    }
}