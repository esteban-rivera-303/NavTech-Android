package com.zanaaustralis.feature_auth.presentation.pin.change

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zanaaustralis.feature_auth.data.PinRepository
import com.zanaaustralis.feature_auth.presentation.ChangePinStep
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author esteban.rivera01
 */

@HiltViewModel
class ChangePinViewModel @Inject constructor(
    private val pinRepository: PinRepository
) : ViewModel() {

    private val _step = MutableStateFlow(ChangePinStep.VERIFY_CURRENT)
    val step: StateFlow<ChangePinStep> = _step

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var newPin: String? = null

    fun onCurrentPinValidated() {
        _error.value = null
        _step.value = ChangePinStep.ENTER_NEW
    }

    fun onNewPinEntered(pin: String) {
        if (!isValidPin(pin)) {
            _error.value = "El PIN debe tener 6 dígitos"
            return
        }
        newPin = pin
        _step.value = ChangePinStep.CONFIRM_NEW
    }

    fun onNewPinConfirmed(pin: String, onSuccess: () -> Unit) {
        if (pin != newPin) {
            _error.value = "Los PIN no coinciden"
            _step.value = ChangePinStep.ENTER_NEW
            newPin = null
            return
        }

        viewModelScope.launch {
            pinRepository.savePin(pin)
            _step.value = ChangePinStep.DONE
            onSuccess()
        }
    }

    fun clearError() {
        _error.value = null
    }

    private fun isValidPin(pin: String) =
        pin.length == 6 && pin.all { it.isDigit() }
}
