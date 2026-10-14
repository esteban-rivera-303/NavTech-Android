package com.zanaaustralis.feature_challanges.presentation.challenge_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zanaaustralis.core.common.Resource
import com.zanaaustralis.feature_challanges.data.model.LocationData
import com.zanaaustralis.feature_challanges.domain.usecase.ChallengeUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author esteban.rivera01
 */

@HiltViewModel
class ChallengeDetailViewModel @Inject constructor(
    private val challengeUseCases: ChallengeUseCases

) : ViewModel() {

    private val _state = MutableStateFlow<Resource<Boolean>>(Resource.Loading)
    val state = _state.asStateFlow()

/*    private val _totpValidation = MutableStateFlow<Resource<Boolean>?>(null)
    val totpValidation = _totpValidation.asStateFlow()

    fun validateTotp(deviceUuid: String, code: String) {
        viewModelScope.launch {
            _totpValidation.value = Resource.Loading
            _totpValidation.value = challengeUseCases.validateTotp.invoke(deviceUuid, code)
        }
    }*/

    fun respondChallenge(
        challengeId: String,
        totpCode: String? = null,
        location: Pair<Double, Double>? = null,
        biometricUsed: Boolean = false
    ) {
        viewModelScope.launch {

            _state.value = Resource.Loading
            _state.value = challengeUseCases.respondChallenge.approve(
                challengeId = challengeId,
                totpCode = totpCode,
                location = location?.let {
                    LocationData(
                        latitude = it.first,
                        longitude = it.second,
                        accuracy = 10.0
                    )
                },
                metadata = mapOf(
                    "biometric_used" to biometricUsed.toString(),
                    "biometric_type" to "fingerprint"
                ))
        }
    }
}
