package com.zanaaustralis.argos.notification

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.zanaaustralis.argos.MainActivity
import com.zanaaustralis.feature_auth.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * @author esteban.rivera01
 */


@AndroidEntryPoint
class ArgosFirebaseService : FirebaseMessagingService() {


    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data

        // TODO for the future
        when (data["type"]) {
            "challenge" -> handleChallengePush(data)
            "identity_validation" -> handleSecurityAlert(data)
            else -> Unit
        }

    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun handleChallengePush(data: Map<String, String>) {
        if (data["type"] != "challenge") return

        val challengeId = data["challenge_id"] ?: return
        val title = data["title"] ?: "Autorización requerida"
        val body = data["body"] ?: "Tienes un desafío pendiente"

        showChallengeNotification(
            challengeId = challengeId,
            title = title,
            body = body
        )

        if (!challengeId.isNullOrBlank()) {
            CoroutineScope(Dispatchers.Main).launch {
                NotificationEventBus.emitChallenge(challengeId)
            }
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun handleSecurityAlert(data: Map<String, String>) {
        val validationId = data["validation_id"] ?: return
        val title = data["title"] ?: "Validación de Identidad"
        val body = data["body"] ?: "Hemos validado correctamente tu identidad"

        showValidationIdentityNotification(
            validationId = validationId,
            title = title,
            body = body
        )
    }


    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showChallengeNotification(
        challengeId: String,
        title: String,
        body: String
    ) {
        val intent = MainActivity.notificationIntent(this).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_CHALLENGE_ID, challengeId)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            challengeId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, "challenges")
            .setSmallIcon(com.zanaaustralis.argos.R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        NotificationManagerCompat.from(this)
            .notify(challengeId.hashCode(), notification)
    }


    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showValidationIdentityNotification(
        validationId: String,
        title: String,
        body: String
    ) {
        val intent = MainActivity.notificationIntent(this).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("validation_id", validationId)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            validationId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, "validations")
            .setSmallIcon(R.drawable.baseline_verified_user_24)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        NotificationManagerCompat.from(this)
            .notify(validationId.hashCode(), notification)

    }
}


object NotificationEventBus {
    private val _challengeEvents = MutableSharedFlow<String>()
    val challengeEvents = _challengeEvents.asSharedFlow()

    suspend fun emitChallenge(challengeId: String) {
        _challengeEvents.emit(challengeId)
    }
}
