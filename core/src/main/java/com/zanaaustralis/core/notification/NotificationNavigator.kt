package com.zanaaustralis.core.notification

import android.app.PendingIntent
import android.content.Context

/**
 * @author esteban.rivera01
 */
interface NotificationNavigator {
    fun createChallengeIntent(context: Context, challengeId: String): PendingIntent
}
