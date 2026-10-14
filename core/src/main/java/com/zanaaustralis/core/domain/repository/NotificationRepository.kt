package com.zanaaustralis.core.domain.repository

import com.zanaaustralis.core.common.Resource
import com.zanaaustralis.core.domain.model.NotificationItem

/**
 * @author esteban.rivera01
 */

interface NotificationRepository {
    suspend fun getNotificationHistory(
        limit: Int,
        offset: Int
    ): Resource<List<NotificationItem>>

    suspend fun markAsRead(notificationIds: List<String>): Resource<Int>
    suspend fun markAllAsRead(): Resource<Int>
}