package com.zanaaustralis.core.domain.model

import com.zanaaustralis.core.data.remote.model.NotificationDto

/**
 * @author esteban.rivera01
 */

data class NotificationItem(
    val id: String,
    val title: String,
    val body: String,
    val appName: String?,
    val type: String?,
    val riskLevel: String?,
    val status: String,
    val createdAt: String,
    val isRead: Boolean,
    val readAt: String?
)

fun NotificationDto.toNotificationItem(): NotificationItem {
    return NotificationItem(
        id = id,
        title = title,
        body = body,
        appName = notification_data?.application_name,
        type = notification_data?.challenge_type,
        riskLevel = notification_data?.risk_level,
        status = status,
        createdAt = created_at,
        isRead = is_read,
        readAt = read_at
    )
}