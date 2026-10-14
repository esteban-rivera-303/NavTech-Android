package com.zanaaustralis.argos.drawer_menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zanaaustralis.core.session.SessionManagerImpl
import com.zanaaustralis.feature_auth.presentation.logout.LogoutUseCase
import com.zanaaustralis.feature_challanges.presentation.challenge_list.HomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author esteban.rivera01
 */

@HiltViewModel
class DrawerViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase,
    private val sessionManager: SessionManagerImpl,
) : ViewModel() {

    private val _logoutState = MutableStateFlow<LogoutState>(LogoutState.Idle)
    val logoutState: StateFlow<LogoutState> = _logoutState

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        loadUserData()
    }

    fun logout() {
        viewModelScope.launch {
            _logoutState.value = LogoutState.Loading

            val result = logoutUseCase()

            _logoutState.value =
                if (result.isSuccess) LogoutState.Success
                else LogoutState.Error("No se pudo cerrar la sesión")
        }
    }

    private fun loadUserData() {
        viewModelScope.launch {
            val user = sessionManager.getUserData()

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                userName = user?.full_name
            )
        }
    }

    fun resetState() {
        _logoutState.value = LogoutState.Idle
    }
}

sealed class LogoutState {
    object Idle : LogoutState()
    object Loading : LogoutState()
    object Success : LogoutState()
    data class Error(val message: String) : LogoutState()
}
