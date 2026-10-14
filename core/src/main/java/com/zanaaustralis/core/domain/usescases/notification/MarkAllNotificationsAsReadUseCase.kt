package com.zanaaustralis.core.domain.usescases.notification

import com.zanaaustralis.core.common.Resource
import com.zanaaustralis.core.domain.repository.NotificationRepository
import javax.inject.Inject

/**
 * @author esteban.rivera01
 */

class MarkAllNotificationsAsReadUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(): Resource<Int> {
        return repository.markAllAsRead()
    }
}