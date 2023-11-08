package apptentive.com.app

import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.RingtoneManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import apptentive.com.android.feedback.Apptentive
import apptentive.com.android.util.InternalUseOnly
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.PUSH_NOTIFICATION
import com.apptentive.apptentive_example.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class AppFirebaseMessagingService : FirebaseMessagingService() {
    @InternalUseOnly
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.i(
            PUSH_NOTIFICATION,
            "Firebase instance token: $token"
        )
        Apptentive.setPushNotificationIntegration(this, Apptentive.PUSH_PROVIDER_APPTENTIVE, token)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val data = remoteMessage.data
        if (isAppInBackground() && Apptentive.isApptentivePushNotification(data)) {
            Apptentive.buildPendingIntentFromPushNotification(this, { pendingIntent ->
                if (pendingIntent != null) {
                    val title = Apptentive.getTitleFromApptentivePush(data)
                    val body = Apptentive.getBodyFromApptentivePush(data)

                    // IMPORTANT: you need to create a notification channel for Android-O
                    val notificationManager = getSystemService(NotificationManager::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createNotificationChannel(notificationManager)
                    val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
                        // Make sure that your icon follows Google's Guidelines : a white icon with transparent background
                        .setSmallIcon(R.drawable.ic_apptentive_notification)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent)
                    // Use APPTENTIVE_NOTIFICATION_ID to dismiss relevant notifications when Message Center is shown
                    notificationManager.notify(Apptentive.APPTENTIVE_NOTIFICATION_ID, notificationBuilder.build())
                } else {
                    // Push notification was not for the active conversation. Do nothing.
                }
            }, data)
        } else {
            // This push did not come from Apptentive. It should be handled by your app.
        }
    }

    /**
     * Help manage whether notifications show in the foreground
     */
    private fun isAppInBackground(): Boolean {
        return ActivityManager.RunningAppProcessInfo().run {
            ActivityManager.getMyMemoryState(this)
            importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
        }
    }

    /**
     * Create the NotificationChannel, but only on API 26+ because
     * the NotificationChannel class is new and not in the support library
     */
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =  NotificationChannel(CHANNEL_ID, "Message Center", NotificationManager.IMPORTANCE_HIGH)
            channel.description = "A messaging service to communicate with the company."
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "com.apptentive.NOTIFICATION_CHANNEL_MESSAGE_CENTER"
    }
}
