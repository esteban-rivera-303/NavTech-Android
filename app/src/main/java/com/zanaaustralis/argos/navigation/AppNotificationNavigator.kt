package com.zanaaustralis.argos.navigation

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.zanaaustralis.argos.MainActivity
import com.zanaaustralis.core.notification.NotificationNavigator
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author esteban.rivera01
 */
@Singleton
class AppNotificationNavigator @Inject constructor() : NotificationNavigator {
    override fun createChallengeIntent(context: Context, challengeId: String): PendingIntent {
        val intent = MainActivity.notificationIntent(context).apply {
            putExtra(MainActivity.EXTRA_CHALLENGE_ID, challengeId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        return PendingIntent.getActivity(
            context,
            challengeId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}