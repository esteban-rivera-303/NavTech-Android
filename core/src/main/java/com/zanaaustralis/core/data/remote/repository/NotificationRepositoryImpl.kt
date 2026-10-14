package com.zanaaustralis.core.data.remote.repository

import com.zanaaustralis.core.common.Resource
import com.zanaaustralis.core.data.remote.AuthApiService
import com.zanaaustralis.core.data.remote.model.MarkReadRequest
import com.zanaaustralis.core.di.AuthenticatedApi
import com.zanaaustralis.core.domain.model.NotificationItem
import com.zanaaustralis.core.domain.model.toNotificationItem
import com.zanaaustralis.core.domain.repository.NotificationRepository
import javax.inject.Inject

/**
 * @author esteban.rivera01
 */

class NotificationRepositoryImpl @Inject constructor(
    @AuthenticatedApi private val api: AuthApiService
) : NotificationRepository {

    override suspend fun getNotificationHistory(
        limit: Int,
        offset: Int
    ): Resource<List<NotificationItem>> {

        return try {

            val response = api.getNotificationHistory(limit, offset)

            if (response.isSuccessful) {

                val list = response.body()?.notifications
                    ?.map { it.toNotificationItem() }
                    .orEmpty()

                Resource.Success(list)

            } else {
                Resource.Error("Error fetching notifications", response.code())
            }

        } catch (e: Exception) {
            Resource.Error("Network error: ${e.localizedMessage}")
        }
    }

    override suspend fun markAsRead(notificationIds: List<String>): Resource<Int> {

        return try {

            val response = api.markNotificationsAsRead(
                MarkReadRequest(notificationIds)
            )

            if (response.isSuccessful) {

                val count = response.body()?.markedCount ?: 0

                Resource.Success(count)

            } else {
                Resource.Error(
                    "Error marking notifications as read",
                    response.code()
                )
            }

        } catch (e: Exception) {
            Resource.Error("Network error: ${e.localizedMessage}")
        }
    }

    override suspend fun markAllAsRead(): Resource<Int> {

        return try {

            val response = api.markAllNotificationsAsRead()

            if (response.isSuccessful) {

                val count = response.body()?.markedCount ?: 0

                Resource.Success(count)

            } else {
                Resource.Error(
                    "Error marking all notifications",
                    response.code()
                )
            }

        } catch (e: Exception) {
            Resource.Error("Network error: ${e.localizedMessage}")
        }
    }


}