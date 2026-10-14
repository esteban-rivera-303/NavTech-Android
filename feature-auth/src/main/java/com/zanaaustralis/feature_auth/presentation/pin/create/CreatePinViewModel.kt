package com.zanaaustralis.feature_auth.presentation.pin.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zanaaustralis.feature_auth.data.PinRepository
import com.zanaaustralis.feature_auth.presentation.pin.CreatePinStep
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author esteban.rivera01
 */

@HiltViewModel
class CreatePinViewModel @Inject constructor(
    private val pinRepository: PinRepository
) : ViewModel() {

    private val _step = MutableStateFlow(CreatePinStep.ENTER)
    val step: StateFlow<CreatePinStep> = _step

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var firstPin: String? = null

    fun onPinCompleted(pin: String, onSuccess: () -> Unit) {
        _error.value = null

        if (!isValidPin(pin)) {
            _error.value = "El PIN debe tener 6 dígitos"
            return
        }

        when (_step.value) {
            CreatePinStep.ENTER -> {
                firstPin = pin
                _step.value = CreatePinStep.CONFIRM
            }

            CreatePinStep.CONFIRM -> {
                if (pin != firstPin) {
                    _error.value = "Los PIN no coinciden"
                    _step.value = CreatePinStep.ENTER
                    firstPin = null
                    return
                }

                viewModelScope.launch {
                    pinRepository.savePin(pin)
                    onSuccess()
                }
            }
        }
    }

    fun onCancel() {
        firstPin = null
        _step.value = CreatePinStep.ENTER
        _error.value = null
    }

    private fun isValidPin(pin: String): Boolean =
        pin.length == 6 && pin.all { it.isDigit() }
}

/*@HiltViewModel
class CreatePinViewModel @Inject constructor(
    private val pinRepository: PinRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CreatePinState())
    val state: StateFlow<CreatePinState> = _state

    fun onPinCompleted(pin: String) {
        _state.update { current ->
            // Limpia error previo
            current.copy(error = null)
        }

        if (!isValidPin(pin)) {
            _state.update {
                it.copy(error = "El PIN debe tener 6 dígitos")
            }
            return
        }

        when (_state.value.step) {
            CreatePinStep.ENTER -> {
                _state.update {
                    it.copy(
                        step = CreatePinStep.CONFIRM,
                        firstPin = pin,
                        error = null
                    )
                }
            }

            CreatePinStep.CONFIRM -> {
                val first = _state.value.firstPin
                if (pin != first) {
                    _state.update {
                        it.copy(
                            step = CreatePinStep.ENTER,
                            firstPin = null,
                            error = "Los PIN no coinciden"
                        )
                    }
                    return
                }

                savePin(pin)
            }
        }
    }

    private fun savePin(pin: String) {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }

            runCatching {
                pinRepository.savePin(pin)
            }.onSuccess {
                _state.update {
                    it.copy(
                        isSaving = false,
                        isCompleted = true
                    )
                }
            }.onFailure {
                _state.update {
                    it.copy(
                        isSaving = false,
                        error = "No se pudo guardar el PIN"
                    )
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun reset() {
        _state.value = CreatePinState()
    }

    private fun isValidPin(pin: String): Boolean =
        pin.length == 4 && pin.all { it.isDigit() }
}*/


data class CreatePinState(
    val step: CreatePinStep = CreatePinStep.ENTER,
    val firstPin: String? = null,
    val error: String? = null,
    val isCompleted: Boolean = false,
    val isSaving: Boolean = false
)