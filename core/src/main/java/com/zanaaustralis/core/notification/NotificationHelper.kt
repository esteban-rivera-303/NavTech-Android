package com.zanaaustralis.core.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.zanaaustralis.core.R
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author esteban.rivera01
 */

@Singleton
class NotificationHelper @Inject constructor(
    private val context: Context,
    private val navigator: NotificationNavigator
) {
    private val channelId = "argos_challenges"

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Desafíos Argos",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showNewChallengeNotification(title: String, description: String, challengeId: String) {
        val pendingIntent = navigator.createChallengeIntent(context, challengeId)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(description)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(challengeId.hashCode(), notification)
    }
}