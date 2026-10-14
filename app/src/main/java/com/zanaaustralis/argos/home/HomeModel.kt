package com.zanaaustralis.argos.home

import com.zanaaustralis.feature_challanges.domain.model.ChallengeItem

/**
 * @author esteban.rivera01
 */



data class HomeUiState(
    val isLoading: Boolean = false,
    val challenges: List<ChallengeItem> = emptyList(),
    val error: String? = null
)
