package com.zanaaustralis.feature_auth.presentation.pin.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zanaaustralis.feature_auth.data.PinRepository
import com.zanaaustralis.feature_auth.data.PinVerificationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author esteban.rivera01
 */

@HiltViewModel
class PinViewModel @Inject constructor(
    private val pinRepository: PinRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PinUiState())
    val uiState: StateFlow<PinUiState> = _uiState

    init {
        loadPersistedAttempts()
    }

    fun validatePin(pin: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (_uiState.value.blocked) return@launch

            _uiState.value = _uiState.value.copy(loading = true, error = null)

            when (val result = pinRepository.verifyPin(pin)) {
                PinVerificationResult.Valid -> {
                    _uiState.value = PinUiState()
                    onSuccess()
                }

                is PinVerificationResult.Invalid -> {
                    _uiState.value = _uiState.value.copy(
                        loading = false,
                        remainingAttempts = result.remainingAttempts,
                        blocked = false,
                        error = "PIN incorrecto. Te quedan ${result.remainingAttempts} intentos."
                    )
                }

                PinVerificationResult.Locked -> _uiState.value = lockedState()
            }
        }
    }

    /** Recarga el estado desde el almacenamiento: un PIN bloqueado sigue bloqueado. */
    fun reset() {
        loadPersistedAttempts()
    }

    private fun loadPersistedAttempts() {
        viewModelScope.launch {
            val remaining = pinRepository.remainingAttempts()
            _uiState.value = if (remaining > 0) {
                PinUiState(remainingAttempts = remaining)
            } else {
                lockedState()
            }
        }
    }

    private fun lockedState() = PinUiState(
        loading = false,
        remainingAttempts = 0,
        blocked = true,
        error = "Has agotado los intentos. Debes restaurar tu PIN."
    )
}

/*@HiltViewModel
class PinViewModel @Inject constructor(
    private val pinRepository: PinRepository
) : ViewModel() {

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    fun validatePin(
        pin: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            val isValid = pinRepository.verifyPin(pin)

            _loading.value = false

            if (isValid) {
                onSuccess()
            } else {
                _error.value = "PIN incorrecto"
            }
        }
    }
}*/
