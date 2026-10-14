package com.zanaaustralis.core.domain.usescases.notification

import com.zanaaustralis.core.common.Resource
import com.zanaaustralis.core.domain.model.NotificationItem
import com.zanaaustralis.core.domain.repository.NotificationRepository
import javax.inject.Inject

/**
 * @author esteban.rivera01
 */
class GetNotificationHistoryUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(
        limit: Int = 50,
        offset: Int = 0
    ): Resource<List<NotificationItem>> {
        return repository.getNotificationHistory(limit, offset)
    }
}