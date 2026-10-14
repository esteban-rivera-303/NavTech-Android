package com.zanaaustralis.feature_challanges.presentation.challenge_list

/**
 * @author esteban.rivera01
 */

/*
@HiltViewModel
class ChallengeViewModel2 @Inject constructor(
    private val sessionProvider: SessionProvider,
    private val getPendingChallengesUseCase: GetPendingChallengesUseCase,
    private val localAuthStore: ChallengeLocalAuthStore,
    private val respondChallenge: RespondChallengeUseCase,
) : ViewModel() {

    private val _challenges = MutableStateFlow<List<ChallengeItem>>(emptyList())
    val challenges: StateFlow<List<ChallengeItem>> = _challenges

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _authorizationStep = MutableSharedFlow<AuthorizationStep>()
    val authorizationStep = _authorizationStep.asSharedFlow()

    private var hasLoadedOnce = false

    init {
        loadChallenges()
    }

    fun loadChallenges(force: Boolean = false) {
        if (hasLoadedOnce && !force) return
        hasLoadedOnce = true

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val accessToken = sessionProvider.getAccessToken()
            if (accessToken == null) {
                _errorMessage.value = "Sesión no encontrada. Inicia sesión nuevamente."
                _isLoading.value = false
                return@launch
            }

            when (val result = getPendingChallengesUseCase(accessToken)) {
                is Resource.Success -> {
                    val list = result.data ?: emptyList()
                    val merged = list.map { ch ->
                        val localFactors = localAuthStore.getAuthorizedFactors(ch.id)

                        ch.copy(
                            authorizedFactors = (ch.authorizedFactors + localFactors).toMutableSet()
                        ).recomputeCritical()
                    }

                    _challenges.value = merged
                }
                is Resource.Error -> {
                    _errorMessage.value = result.error ?: "Error al cargar desafíos."
                }
                else -> Unit
            }

            _isLoading.value = false
        }
    }

    fun requestNextStep(challengeItem: ChallengeItem) {
        viewModelScope.launch {
            val challenge = _challenges.value.firstOrNull { it.id == challengeItem.id } ?: return@launch

            val next = computeNextRequiredStep(challenge)
            _authorizationStep.emit(next)
        }
    }

    private fun computeNextRequiredStep(ch: ChallengeItem): AuthorizationStep {
        return when {
            ch.requiresBiometric && !ch.authorizedFactors.contains(AuthFactor.BIOMETRIC) ->
                AuthorizationStep.RequireBiometric(ch.id)

            ch.isCritical && !ch.authorizedFactors.contains(AuthFactor.PIN) ->
                AuthorizationStep.RequirePin(ch.id)

            ch.requiresTotp && !ch.authorizedFactors.contains(AuthFactor.TOTP) ->
                AuthorizationStep.RequireTotp(ch.id)

            ch.requiresLocation && !ch.authorizedFactors.contains(AuthFactor.LOCATION) ->
                AuthorizationStep.RequireLocation(ch.id)

            else ->
                AuthorizationStep.ReadyToAuthorize(ch.id)
        }
    }


    fun updateChallengeStatus(id: String, newStatus: String) {
        _challenges.update { list ->
            list.map { challenge ->
                if (challenge.id == id) {
                    challenge.copy(status = newStatus)
                } else {
                    challenge
                }
            }
        }
    }

    fun markFactorAsAuthorized(id: String, factor: AuthFactor) {
        viewModelScope.launch {
            _challenges.update { list ->
                list.map {
                    if (it.id == id) it.markFactorAsAuthorized(factor) else it
                }
            }

            val updated = _challenges.value.firstOrNull { it.id == id } ?: return@launch
            localAuthStore.setAuthorizedFactors(id, updated.authorizedFactors)

            // Luego de marcar factor, pide el siguiente step
            _authorizationStep.emit(computeNextRequiredStep(updated))
        }
    }

    fun authorizeChallenge(challengeId: String) {
        viewModelScope.launch {
            val challenge = _challenges.value.firstOrNull { it.id == challengeId } ?: return@launch

            // Validación: no dejes enviar si faltan factores
            val next = computeNextRequiredStep(challenge)
            if (next !is AuthorizationStep.ReadyToAuthorize) {
                _authorizationStep.emit(next)
                return@launch
            }

            updateChallengeStatus(challengeId, "enviando")

            if (sessionProvider.getAccessToken() == null) {
                updateChallengeStatus(challengeId, "pending")
                _errorMessage.value = "Sesión no encontrada."
                return@launch
            }

            when (val result = respondChallenge.approve(
                challengeId = challenge.id,
                totpCode = null,//pendingTotpCodes[challenge.id],
                location = null,//pendingLocations[challenge.id],
                metadata = null,//challenge.toMetadata()

            )) {
                is Resource.Success -> {
                    updateChallengeStatus(challengeId, "enviado")
                    localAuthStore.clear(challengeId) // ya no lo necesitas localmente
                    _authorizationStep.emit(AuthorizationStep.Completed(challengeId))
                }
                is Resource.Error -> {
                    updateChallengeStatus(challengeId, "pending")
                    _errorMessage.value = result.error ?: "Error autorizando desafío."
                }
                else -> Unit
            }
        }
    }



    fun removeChallenge(id: String) {
        _challenges.update { list ->
            list.filterNot { it.id == id }
        }
    }
}
*/
