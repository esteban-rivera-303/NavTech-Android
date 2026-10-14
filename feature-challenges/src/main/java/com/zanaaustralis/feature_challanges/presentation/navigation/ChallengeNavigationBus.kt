package com.zanaaustralis.feature_challanges.presentation.navigation

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * @author esteban.rivera01
 */

object ChallengeNavigationBus {
    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    fun emit(challengeId: String) {
        _events.tryEmit(challengeId)
    }
}
