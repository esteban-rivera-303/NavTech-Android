package com.zanaaustralis.feature_challanges.presentation.challenge_list

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zanaaustralis.core.common.Resource
import com.zanaaustralis.core.location.LocationMetadata
import com.zanaaustralis.core.location.LocationProvider
import com.zanaaustralis.core.secure.crypto.UserAuthKeyStore
import com.zanaaustralis.core.secure.crypto.UserAuthSignature
import com.zanaaustralis.core.session.SessionManagerImpl
import com.zanaaustralis.core.session.SessionStateNotification
import com.zanaaustralis.feature_auth.AuthFactor
import com.zanaaustralis.feature_auth.ChallengeLocalAuthStore
import com.zanaaustralis.feature_auth.data.local.TotpManager
import com.zanaaustralis.feature_auth.presentation.totp.ObserveTotpUseCase
import com.zanaaustralis.feature_challanges.data.model.LocationData
import com.zanaaustralis.feature_challanges.domain.AuthorizationStep
import com.zanaaustralis.feature_challanges.domain.model.ChallengeItem
import com.zanaaustralis.feature_challanges.domain.model.isReadyToAuthorize
import com.zanaaustralis.feature_challanges.domain.model.markFactorAsAuthorized
import com.zanaaustralis.feature_challanges.domain.model.missingFactors
import com.zanaaustralis.feature_challanges.domain.model.needsBiometricSignature
import com.zanaaustralis.feature_challanges.domain.usecase.GetPendingChallengesUseCase
import com.zanaaustralis.feature_challanges.domain.usecase.RespondChallengeUseCase
import com.zanaaustralis.feature_challanges.presentation.challenge_detail.ChallengeDetailState
import com.zanaaustralis.feature_challanges.presentation.challenge_detail.ChallengePermissionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.security.Signature
import javax.inject.Inject

/**
 * @author esteban.rivera01
 */

@HiltViewModel
class ChallengeViewModel @Inject constructor(
    private val sessionManager: SessionManagerImpl,
    private val getPendingChallengesUseCase: GetPendingChallengesUseCase,
    private val localAuthStore: ChallengeLocalAuthStore,
    private val respondChallengeUseCase: RespondChallengeUseCase,
    private val observeTotpUseCase: ObserveTotpUseCase,
    private val totpManager: TotpManager,
    private val userAuthKeyStore: UserAuthKeyStore
) : ViewModel() {

    val session: StateFlow<SessionStateNotification> = sessionManager.sessionNotificationState
    private val _isInDetailFlow = MutableStateFlow(false)

    private val _challenges = MutableStateFlow<List<ChallengeItem>>(emptyList())
    val challenges: StateFlow<List<ChallengeItem>> = _challenges

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _authorizationStep = MutableSharedFlow<AuthorizationStep>()
    val authorizationStep = _authorizationStep.asSharedFlow()

    private val _permissionState =
        MutableStateFlow<ChallengePermissionState>(ChallengePermissionState.Idle)

    val permissionState: StateFlow<ChallengePermissionState> = _permissionState

    private val _locationMetadata =
        MutableStateFlow<Map<String, LocationMetadata>>(emptyMap())

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    private val _detailState =
        MutableStateFlow<ChallengeDetailState>(ChallengeDetailState.Idle)

    val detailState: StateFlow<ChallengeDetailState> = _detailState
    private val mutex = Mutex()

    init {
        loadUserData()
        loadChallenges()
        startPolling()
    }

    val totpState: StateFlow<TotpState> =
        observeTotpUseCase()
            .map { state ->
                TotpState(
                    code = state.code,
                    secondsRemaining = state.secondsRemaining.toInt(),
                    isActive = state.code.isNotEmpty()
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = TotpState()
            )


    fun loadChallenges() {
        viewModelScope.launch {

            mutex.withLock {
                try {
                    _isLoading.value = true
                    _errorMessage.value = null

                    val accessToken = sessionManager.getSession()?.accessToken
                    if (accessToken == null) {
                        _errorMessage.value = "Sesión no encontrada. Inicia sesión nuevamente."
                        return@launch
                    }

                    when (val result = getPendingChallengesUseCase.invoke(accessToken)) {
                        is Resource.Success -> {
                            val list = result.data ?: emptyList()
                            val merged = list.map { ch ->
                                val localFactors = localAuthStore.locallyValidatedFactors(ch.id)
                                ch.copy(
                                    authorizedFactors = (ch.authorizedFactors + localFactors).toMutableSet()
                                )
                            }
                            _challenges.value = merged
                        }

                        is Resource.Error -> {
                            val isInvalidToken = result.code == 403

                            if (isInvalidToken) {
                                //stopPolling()
                                sessionManager.onInvalidSessionDetected()
                                _errorMessage.value = result.error ?: "Error al cargar desafíos"
                            }
                            else{
                                _errorMessage.value = result.error ?: "Error al cargar desafíos."
                            }
                        }

                        else -> Unit
                    }
                } finally {
                    _isLoading.value = false
                }
            }
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

    fun onEnterChallengeDetail(
        challengeId: String,
        hasLocationPermission: Boolean
    ) {
        _isInDetailFlow.value = true
        //_isLoading.value = true
        val challenge = _challenges.value.firstOrNull { it.id == challengeId }
            ?: return

        if (challenge.requiresLocation && !challenge.authorizedFactors.contains(AuthFactor.LOCATION)
        ) {
            if (!hasLocationPermission) {
                _permissionState.value =
                    ChallengePermissionState.RequireLocationPermission
            } else {
                _permissionState.value =
                    ChallengePermissionState.RequestLocation
            }
        }
    }

    fun fetchLocation(
        challengeId: String,
        locationProvider: LocationProvider
    ) {
        viewModelScope.launch {
            val location = locationProvider.getLastLocation()

            if (location != null) {
                val metadata = LocationMetadata(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    accuracy = location.accuracy
                )

                _locationMetadata.update { map ->
                    map + (challengeId to metadata)
                }

                markFactorAsAuthorized(
                    challengeId,
                    AuthFactor.LOCATION
                )

                _permissionState.value = ChallengePermissionState.Ready
            } else {
                _permissionState.value =
                    ChallengePermissionState.Error(
                        "No se pudo obtener la ubicación"
                    )
            }
        }
    }


    private var pollingJob: Job? = null

    fun startPolling() {
        if (pollingJob?.isActive == true ) return

        pollingJob = viewModelScope.launch {
            while (isActive && !_isInDetailFlow.value) {
                Log.e("Challenges", "<<< polling cargando desafios !!!")
                loadChallenges()
                delay(
                    if (_errorMessage.value != null) 30_000
                    else 10_000
                )
            }
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
    }


    fun requestNextStep(challengeItem: ChallengeItem) {
        viewModelScope.launch {
            val challenge = _challenges.value.firstOrNull { it.id == challengeItem.id } ?: return@launch
            val next = computeNextRequiredStep(challenge)
            _authorizationStep.emit(next)
        }
    }

    fun requestNextStep(
        challengeId: String,
        forcedFactor: AuthFactor? = null
    ) {
        viewModelScope.launch {

            val challenge = _challenges.value
                .firstOrNull { it.id == challengeId }
                ?: return@launch

            val deferBiometric = forcedFactor == AuthFactor.BIOMETRIC && challenge.needsBiometricSignature()
            val step = if (forcedFactor != null && !deferBiometric) {
                mapFactorToStep(challengeId, forcedFactor)
            } else {
                computeNextRequiredStep(challenge)
            }

            _authorizationStep.emit(step)
        }
    }

    private fun mapFactorToStep(
        challengeId: String,
        factor: AuthFactor
    ): AuthorizationStep {

        return when (factor) {
            AuthFactor.BIOMETRIC -> AuthorizationStep.RequireBiometric(challengeId)
            AuthFactor.PIN -> AuthorizationStep.RequirePin(challengeId)
            AuthFactor.TOTP -> AuthorizationStep.RequireTotp(challengeId)
            AuthFactor.LOCATION -> AuthorizationStep.RequireLocation(challengeId)
        }
    }

    fun authorizeChallenge(challengeId: String) {
        viewModelScope.launch {
            val challenge = _challenges.value.firstOrNull { it.id == challengeId }
                ?: return@launch

            if (!challenge.isReadyToAuthorize() || challenge.needsBiometricSignature()) {
                _authorizationStep.emit(
                    computeNextRequiredStep(challenge)
                )
                return@launch
            }

            processChallenge(
                challenge = challenge,
                response = ChallengeResponse.APPROVED
            )
        }
    }

    /** Firma v4: el prompt biométrico desbloqueó [signature]; se aprueba con ella. */
    fun approveWithBiometricSignature(challengeId: String, signature: Signature) {
        viewModelScope.launch {
            val challenge = _challenges.value.firstOrNull { it.id == challengeId }
                ?: return@launch

            processChallenge(
                challenge = challenge,
                response = ChallengeResponse.APPROVED,
                userAuthSignature = signature
            )
        }
    }

    fun rejectChallenge(challengeId: String) {
        viewModelScope.launch {
            val challenge = _challenges.value.firstOrNull { it.id == challengeId }
                ?: return@launch

            processChallenge(
                challenge = challenge,
                response = ChallengeResponse.REJECTED
            )
        }
    }

    fun onLocationPermissionResult(granted: Boolean) {
        _permissionState.value = if (granted) {
            ChallengePermissionState.RequestLocation
        } else {
            ChallengePermissionState.Error("Se requiere ubicación para continuar")
        }
    }
    private val _totpByChallenge = MutableStateFlow<Map<String, String>>(emptyMap())
    fun onTotpEntered(challengeId: String, code: String) {
        viewModelScope.launch {
            val expected = totpManager.getCurrentCode()
            if (code == expected) {
                /*_authorizationStep.emit(
                    AuthorizationStep.Failed("Código incorrecto")
                )
                return@launch*/
                _totpByChallenge.update {
                    it + (challengeId to code)
                }
                // ✔ marcar factor
                markFactorAsAuthorized(challengeId, AuthFactor.TOTP)
                requestNextStep(challengeId)
            }
        }
    }

    private suspend fun processChallenge(
        challenge: ChallengeItem,
        response: ChallengeResponse,
        userAuthSignature: Signature? = null,
    ) {
        _isInDetailFlow.value = true
        _detailState.value = ChallengeDetailState.Sending
        _errorMessage.value = ""
        val challengeId = challenge.id

        _challenges.update { list ->
            list.map {
                if (it.id == challengeId) it.copy(status = "enviando") else it
            }
        }

        val location =
            if (response == ChallengeResponse.APPROVED && challenge.requiresLocation)
                _locationMetadata.value[challengeId]
            else null


        val geo: LocationData? = location?.let {
            LocationData(
                latitude = it.latitude,
                longitude = it.longitude,
                accuracy = it.accuracy.toDouble()
            )
        }

        val metadata =
            if (response == ChallengeResponse.APPROVED)
                buildMetadata(challenge, location, biometricSigned = userAuthSignature != null)
            else null
        val totpCode = _totpByChallenge.value[challengeId]
        val result = when (response) {
            ChallengeResponse.APPROVED ->
                respondChallengeUseCase.approve(
                    challengeId = challengeId,
                    totpCode = totpCode,
                    location = geo,
                    metadata = metadata,
                    userAuthSignature = userAuthSignature
                )

            ChallengeResponse.REJECTED ->
                respondChallengeUseCase.reject(
                    challengeId = challengeId,
                    totpCode = null,
                    location = null,
                    metadata = null
                )
        }

        when (result) {
            is Resource.Success -> {
                _authorizationStep.emit(
                    AuthorizationStep.Completed(challengeId)
                )
                localAuthStore.clear(challengeId)
                _challenges.update { list ->
                    list.filterNot { it.id == challengeId }
                }
            }

            is Resource.Error -> {
                _errorMessage.value = result.error

                _authorizationStep.emit(
                    AuthorizationStep.Failed(result.error ?: "Error desconocido")
                )

                _challenges.update { list ->
                    list.map {
                        if (it.id == challengeId) it.copy(status = "pending") else it
                    }
                }
            }

            else -> Unit
        }
    }

    enum class ChallengeResponse {
        APPROVED,
        REJECTED
    }

    private fun buildMetadata(
        challenge: ChallengeItem,
        location: LocationMetadata? = null,
        biometricSigned: Boolean = false
    ): Map<String, String>? {
        val map = mutableMapOf<String, String>()

        if (biometricSigned || challenge.authorizedFactors.contains(AuthFactor.BIOMETRIC)) {
            map["biometric_used"] = "true"
            map["biometric_type"] = "fingerprint"
        }

        if (challenge.authorizedFactors.contains(AuthFactor.PIN)) {
            map["pin_used"] = "true"
        }
        if (location != null) {
            map["location"] = "true"
            map["latitude"] = location.latitude.toString()
            map["longitude"] = location.longitude.toString()
            map["accuracy"] = location.accuracy.toString()
        }

        return map.takeIf { it.isNotEmpty() }
    }


    private fun computeNextRequiredStep(ch: ChallengeItem): AuthorizationStep {
        val pending = ch.missingFactors().firstOrNull()
        return when (pending) {
            AuthFactor.BIOMETRIC -> AuthorizationStep.RequireBiometric(ch.id)
            AuthFactor.PIN -> AuthorizationStep.RequirePin(ch.id)
            AuthFactor.LOCATION -> AuthorizationStep.RequireLocation(ch.id)
            AuthFactor.TOTP -> AuthorizationStep.RequireTotp(ch.id)
            null -> if (ch.needsBiometricSignature()) {
                biometricSignatureStep(ch.id)
            } else {
                AuthorizationStep.ReadyToAuthorize(ch.id)
            }
        }
    }

    private fun biometricSignatureStep(challengeId: String): AuthorizationStep =
        when (val prepared = userAuthKeyStore.prepareSignature()) {
            is UserAuthSignature.Ready ->
                AuthorizationStep.RequireBiometricSignature(challengeId, prepared.signature)

            UserAuthSignature.MissingKey -> AuthorizationStep.Failed(
                "Este dispositivo no tiene una clave biométrica registrada. Vuelve a enrolar el dispositivo."
            )

            UserAuthSignature.Invalidated -> AuthorizationStep.Failed(
                "Tu biometría cambió. Vuelve a enrolar el dispositivo para aprobar desafíos."
            )
        }

    fun markFactorAsAuthorized(id: String, factor: AuthFactor) {
        viewModelScope.launch {
            _challenges.update { list ->
                list.map {
                    if (it.id == id) it.markFactorAsAuthorized(factor)
                    else it
                }
            }

            val updated = _challenges.value.firstOrNull { it.id == id }
                ?: return@launch

            val factorsToPersist = updated.authorizedFactors
                .filterNot { it == AuthFactor.LOCATION }

            localAuthStore.setAuthorizedFactors(id, factorsToPersist.toSet())
        }
    }

    fun removeChallenge(id: String) {
        _challenges.update { list ->
            list.filterNot { it.id == id }
        }
    }
}


sealed class ChallengeAction {
    data class ResolveFactor(val factor: AuthFactor) : ChallengeAction()
    object Authorize : ChallengeAction()
    object Sending : ChallengeAction()
    object None : ChallengeAction()
}

data class HomeUiState(
    val isLoading: Boolean = true,
    val userName: String? = null
)

data class TotpState(
    val code: String = "",
    val secondsRemaining: Int = 0,
    val isActive: Boolean = false
)