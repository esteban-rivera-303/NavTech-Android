package com.zanaaustralis.argos.launch

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.zanaaustralis.core.common.Resource
import com.zanaaustralis.core.common.formatTimeRemaining
import com.zanaaustralis.core.common.formatTimestamp
import com.zanaaustralis.core.device_secure.SecurityManagerNavtech
import com.zanaaustralis.core.domain.repository.AuthRepository
import com.zanaaustralis.core.session.SessionManagerImpl
import com.zanaaustralis.core.store.notification.PushTokenStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author esteban.rivera01
 */

@HiltViewModel
class LaunchViewModel @Inject constructor(
    private val sessionManagerImpl: SessionManagerImpl,
    private val repository: AuthRepository,
    private val pushTokenStore: PushTokenStore,
    private val securityManager: SecurityManagerNavtech
) : ViewModel() {

    private val _uiState = MutableStateFlow(LaunchUiState())
    val uiState: StateFlow<LaunchUiState> = _uiState

    init {
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                onNewFcmToken(token)
            }
        startLaunchFlow()
    }

    private fun setLoadingState(
        isLoading: Boolean,
        success: Boolean = false,
        goToHome: Boolean = false,
        goToOnboarding: Boolean = false
    ) {
        _uiState.value = _uiState.value.copy(
            isLoading = isLoading,
            success = success,
            goToHome = goToHome,
            goToOnboarding = goToOnboarding
        )
    }

    private fun startLaunchFlow() {
        viewModelScope.launch {

            delay(400)

            //val (risk, signals) = securityManager.evaluate()
            //val session = sessionManagerImpl.getSession()

            if (sessionManagerImpl.isSessionActive()) {

                delay(300)

                setLoadingState(
                    isLoading = false,
                    success = true,
                    goToHome = true
                )
                return@launch
            }

            delay(300)
            val response = repository.login()

            if (response is Resource.Success) {
                sessionManagerImpl.updateUnreadNotifications(response.data.has_unread_notifications ?: false)
                delay(300)

                setLoadingState(
                    isLoading = false,
                    success = true,
                    goToHome = true
                )

            } else {
                delay(300)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    success = false,
                    step = LaunchStep.Intro
                )
            }
        }
    }

    fun onNewFcmToken(token: String) {
        viewModelScope.launch {
            pushTokenStore.save(token)
        }
    }
}