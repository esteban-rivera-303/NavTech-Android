package com.zanaaustralis.feature_kyc.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zanaaustralis.core.common.Resource
import com.zanaaustralis.feature_kyc.data.model.InitiateKycResponse
import com.zanaaustralis.feature_kyc.data.model.InitiateKycResponseError
import com.zanaaustralis.feature_kyc.data.model.KycSubmission
import com.zanaaustralis.feature_kyc.data.remote.parseError
import com.zanaaustralis.feature_kyc.domain.KycUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

/**
 * @author esteban.rivera01
 */

@HiltViewModel
class KyCViewModel @Inject constructor(
    private val useCases: KycUseCases
) : ViewModel() {

    data class KyCUiState(
        val isLoading: Boolean = false,
        val error: String? = null,
        val kycUrl: String? = null,
        val status: String? = null,
        val approved: Boolean? = null,
        val isInReview: Boolean? = null,
        val lastSubmission: KycSubmission? = null
    )

    private val _KyC_uiState = MutableStateFlow(KyCUiState(approved = true))
    val uiState = _KyC_uiState.asStateFlow()

    fun initiate(rut: String) {
        viewModelScope.launch {
            try {
                _KyC_uiState.value = KyCUiState(isLoading = true)
                val result = useCases.initiateKyc(rut)

                _KyC_uiState.update {
                    it.copy(
                        kycUrl = result.verification_url,
                        status = result.status,
                        isLoading = false
                    )
                }

            } catch (e: HttpException) {
                val errorJson = e.response()?.errorBody()?.string()
                val result = parseError<InitiateKycResponseError>(errorJson)

                if(result?.error == "You already have a pending KYC verification."){
                    _KyC_uiState.value = KyCUiState(
                        isInReview = true,
                    )
                }
                else if (result?.verification_url != null) {
                    _KyC_uiState.value = KyCUiState(
                        kycUrl = result.verification_url,
                        status = result.status,
                        isLoading = false
                    )
                } else {
                    _KyC_uiState.value = KyCUiState(
                        isLoading = false,
                        error = e.message
                    )
                }

            } catch (e: Exception) {
                _KyC_uiState.value = KyCUiState(
                    isLoading = false,
                    error = e.message ?: "Al parecer tienes una validación pendiente por revisar"
                )
            }
        }
    }

    private fun checkStatus(rut: String) {
        viewModelScope.launch {
            try {
                when (val result = useCases.getKycStatus(rut)) {
                    is Resource.Success -> {
                        val last = result.data.latest_submission

                        val approved = when {
                            result.data.kyc_status == "verified" -> true
                            result.data.kyc_status == "rejected" -> false
                            else -> null
                        }

                        val isInReview = if (result.data.kyc_status == "completed" || result.data.kyc_status == "pending") true  else null

                        _KyC_uiState.value = _KyC_uiState.value.copy(
                            status = result.data.kyc_status,
                            lastSubmission = last,
                            isInReview = isInReview,
                            approved = approved
                        )
                    }

                    is Resource.Error -> {
                        _KyC_uiState.update {
                            it.copy(error = result.error)
                        }
                    }
                    else -> Unit
                }

            } catch (e: Exception) {
                _KyC_uiState.value = _KyC_uiState.value.copy(error = e.message)
            }
        }
    }

    fun startPolling(rut: String) {
        viewModelScope.launch {

            repeat(10) { // intenta 10 veces
                checkStatus(rut)

                val approved = _KyC_uiState.value.approved
                if (approved != null) return@launch

                delay(2000)
            }

            _KyC_uiState.update {
                it.copy(error = "No se pudo validar identidad")
            }
        }
    }
}
