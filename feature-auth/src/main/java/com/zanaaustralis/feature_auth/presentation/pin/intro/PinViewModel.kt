package com.zanaaustralis.feature_auth.presentation.pin.intro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zanaaustralis.feature_auth.data.PinRepository
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

    private val _hasPin = MutableStateFlow<Boolean?>(null)
    val hasPin: StateFlow<Boolean?> = _hasPin

    init {
        viewModelScope.launch {
            _hasPin.value = pinRepository.hasPin()
        }
    }

    fun createPin(pin: String) {
        viewModelScope.launch {
            pinRepository.savePin(pin)
            _hasPin.value = true
        }
    }

    fun clearPin() {
        viewModelScope.launch {
            pinRepository.clearPin()
            _hasPin.value = false
        }
    }
}