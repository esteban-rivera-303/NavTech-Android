package com.zanaaustralis.argos.notification.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zanaaustralis.core.common.Resource
import com.zanaaustralis.core.domain.model.NotificationItem
import com.zanaaustralis.core.domain.usescases.notification.GetNotificationHistoryUseCase
import com.zanaaustralis.core.domain.usescases.notification.MarkAllNotificationsAsReadUseCase
import com.zanaaustralis.core.domain.usescases.notification.MarkNotificationsAsReadUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author esteban.rivera01
 */

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val getNotificationHistoryUseCase: GetNotificationHistoryUseCase,
    private val markAsReadUseCase: MarkNotificationsAsReadUseCase,
    private val markAllUseCase: MarkAllNotificationsAsReadUseCase
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading

    private val _isPaginating = MutableStateFlow(false)
    val isPaginating = _isPaginating

    private val _error = MutableStateFlow<String?>(null)
    val error = _error

    private var offset = 0
    private var hasMore = true

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState

    fun markNotificationAsRead(notificationId: String) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    notifications = state.notifications.map {
                        if (it.id == notificationId) {
                            it.copy(isRead = true)
                        } else it
                    }
                )
            }

            val result = markAsReadUseCase(listOf(notificationId))
            when (result){
                is Resource.Success  -> {
                    _uiState.update { state ->
                        state.copy(
                            notifications = state.notifications.map {
                                if (it.id == notificationId) it.copy(isRead = true)
                                else it
                            }
                        )
                    }
                }

                is Resource.Error -> {
                    _uiState.update { state ->
                        state.copy(
                            notifications = state.notifications.map {
                                if (it.id == notificationId) {
                                    it.copy(isRead = false)
                                } else it
                            }
                        )
                    }
                }
                Resource.Loading -> {

                }
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {

            _uiState.update { it.copy(isMarkingAll = true) }
            _uiState.update { state ->
                state.copy(
                    notifications = state.notifications.map {
                        it.copy(isRead = true)
                    }
                )
            }
            val result = markAllUseCase()

            when (result){
                is Resource.Success  -> {
                    _uiState.update { state ->
                        state.copy(
                            notifications = state.notifications.map {
                                it.copy(isRead = true)
                            }
                        )
                    }
                }

                is Resource.Error -> {}
                Resource.Loading -> {

                }
            }
            _uiState.update { it.copy(isMarkingAll = false) }

        }
    }

    init {
        loadNotifications(reset = true)
    }

    fun loadNotifications(reset: Boolean = false) {

        if (_isLoading.value || _isPaginating.value) return
        if (!hasMore && !reset) return

        viewModelScope.launch {

            if (reset) {
                offset = 0
                hasMore = true
                _notifications.value = emptyList()
                _isLoading.value = true
                _error.value = null
            } else {
                _isPaginating.value = true
            }

            when (val result = getNotificationHistoryUseCase(
                limit = 50,
                offset = offset
            )) {

                is Resource.Success -> {

                    val newItems = result.data ?: emptyList()

                    _notifications.value =
                        if (reset) newItems else _notifications.value + newItems

                    offset += newItems.size
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
}

/*data class NotificationItem(
    val id: String,
    val title: String,
    val isRead: Boolean
)*/

data class NotificationsUiState(
    val notifications: List<NotificationItem> = emptyList(),
    val isMarkingAll: Boolean = false
)