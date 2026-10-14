package com.zanaaustralis.feature_challanges.presentation.challenge_detail

import com.zanaaustralis.core.location.LocationMetadata


/**
 * @author esteban.rivera01
 */

sealed class ChallengePermissionState {
    object Idle : ChallengePermissionState()
    object Ready : ChallengePermissionState()
    object RequestLocation : ChallengePermissionState()
    object RequireLocationPermission : ChallengePermissionState()
    data class Error(val message: String) : ChallengePermissionState()
}
