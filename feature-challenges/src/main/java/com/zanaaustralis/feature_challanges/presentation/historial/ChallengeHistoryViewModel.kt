package com.zanaaustralis.feature_challanges.presentation.historial

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zanaaustralis.core.common.Resource
import com.zanaaustralis.feature_challanges.domain.model.ChallengeItem
import com.zanaaustralis.feature_challanges.domain.usecase.GetChallengeHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author esteban.rivera01
 */
@HiltViewModel
class ChallengeHistoryViewModel @Inject constructor(
    private val getChallengeHistoryUseCase: GetChallengeHistoryUseCase
) : ViewModel() {

    private val _history = MutableStateFlow<List<ChallengeItem>>(emptyList())
    val history: StateFlow<List<ChallengeItem>> = _history

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isPaginating = MutableStateFlow(false)
    val isPaginating: StateFlow<Boolean> = _isPaginating

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _offset = 0
    private var currentOffset = 0

    private var hasMore = true
    private var currentFilter: String? = null

    init {
        loadHistory(reset = true)
    }

    fun loadHistory(
        status: String? = currentFilter,
        reset: Boolean = false
    ) {

        if (_isLoading.value || _isPaginating.value) return
        if (!hasMore && !reset) return

        viewModelScope.launch {

            if (reset) {
                currentOffset = 0
                hasMore = true
                _history.value = emptyList()
                _isLoading.value = true
                currentFilter = status
            } else {
                _isPaginating.value = true
            }

            when (val result = getChallengeHistoryUseCase(
                status = status,
                limit = 20,
                offset = currentOffset
            )) {

                is Resource.Success -> {

                    val newItems = result.data ?: emptyList()

                    _history.value = if (reset) {
                        newItems
                    } else {
                        _history.value + newItems
                    }

                    currentOffset += newItems.size
                    hasMore = newItems.isNotEmpty()
                }

                is Resource.Error -> {
                    _error.value = result.error
                }

                else -> Unit
            }

            _isLoading.value = false
            _isPaginating.value = false
        }
    }

    fun filter(status: String?) {
        loadHistory(status = status, reset = true)
    }
}

fun ChallengeItem.statusLabel(): String {
    return when (status.lowercase()) {
        "approved" -> "Aprobado"
        "rejected" -> "Rechazado"
        "expired" -> "Expirado"
        else -> status
    }
}

fun ChallengeItem.statusColor(): Color {
    return when (status.lowercase()) {
        "approved" -> Color(0xFF4CAF50)
        "rejected" -> Color(0xFFD32F2F)
        "expired" -> Color(0xFFFF9800)
        else -> Color.Gray
    }
}