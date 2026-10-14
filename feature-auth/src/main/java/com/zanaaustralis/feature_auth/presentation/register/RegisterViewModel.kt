package com.zanaaustralis.feature_auth.presentation.register

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zanaaustralis.core.common.Resource
import com.zanaaustralis.core.data.local.UserLocalDataStore
import com.zanaaustralis.core.data.remote.model.RegisterRequest
import com.zanaaustralis.core.domain.usescases.AuthUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author esteban.rivera01
 */

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authUseCases: AuthUseCases,
    private val userLocalDataStore: UserLocalDataStore
) : ViewModel() {

    private val _uiStateRegister = MutableStateFlow(RegisterUiState())
    val uiStateRegister: StateFlow<RegisterUiState> = _uiStateRegister


    fun registerUser(rut: String, email: String, phone: String) {

        viewModelScope.launch {
            _uiStateRegister.value = RegisterUiState(isLoading = true)
            try {
                val request = RegisterRequest(
                    rut = rut,
                    email = email,
                    phone = phone
                )
                when (val result = authUseCases.registerUser(request)) {

                    is Resource.Success -> {
                        _uiStateRegister.value = _uiStateRegister.value.copy(
                            rut = rut,
                            email = email,
                            phone = phone,
                            isLoading = false,
                            success = true,
                            message = "Registro exitoso. Se enviaron los códigos.",
                        )

                        userLocalDataStore.savePreRegistration(
                            rut = rut,
                            email = email,
                            phone = phone
                        )
                    }

                    is Resource.Error -> {
                        _uiStateRegister.value = _uiStateRegister.value.copy(
                            isLoading = false,
                            success = false,
                            message = result.error
                        )
                    }

                    is Resource.Loading -> _uiStateRegister.value = RegisterUiState(isLoading = true)
                }
            } catch (e: Exception) {
                _uiStateRegister.value =_uiStateRegister.value.copy(
                    isLoading = false,
                    success = false,
                    message = e.localizedMessage
                )
            }
        }
    }
}