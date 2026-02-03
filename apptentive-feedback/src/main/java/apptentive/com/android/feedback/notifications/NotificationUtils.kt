package apptentive.com.android.feedback.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import apptentive.com.android.feedback.ApptentiveDefaultClient
import apptentive.com.android.util.InternalUseOnly
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.PUSH_NOTIFICATION
import org.json.JSONException
import org.json.JSONObject

@InternalUseOnly
object NotificationUtils {
    private const val APPTENTIVE_PUSH_EXTRA_KEY = "apptentive"
    internal const val KEY_TOKEN = "token"
    internal const val PUSH_EXTRA_KEY_PARSE = "com.parse.Data"
    internal const val PUSH_EXTRA_KEY_UA = "com.urbanairship.push.EXTRA_PUSH_MESSAGE_BUNDLE"
    internal const val TITLE_DEFAULT = "title"
    internal const val BODY_DEFAULT = "body"
    internal const val BODY_PARSE = "alert"
    internal const val BODY_UA = "com.urbanairship.push.ALERT"

    private fun getPendingMessageCenterNotificationIntent(context: Context): PendingIntent {
        val flags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT

        val messageCenterIntent = Intent(context, ApptentiveNotificationActivity::class.java)
        messageCenterIntent.putExtra(
            ApptentiveNotificationActivity.APPTENTIVE_NOTIFICATION_EVENT,
            ApptentiveNotificationActivity.APPTENTIVE_NOTIFICATION_MESSAGE_CENTER
        )

        Log.d(
            PUSH_NOTIFICATION,
            "Push notification generated $messageCenterIntent with extras ${messageCenterIntent.extras}"
        )

        return PendingIntent.getActivity(context, 0, messageCenterIntent, flags)
    }

    @InternalUseOnly
    fun getApptentivePushNotificationData(intent: Intent?): String? {
        if (intent != null) {
            Log.v(PUSH_NOTIFICATION, "Got an Intent")
            return getApptentivePushNotificationData(intent.extras)
        } else Log.d(PUSH_NOTIFICATION, "No intent received")
        return null
    }

    /**
     * This bundle could be any bundle sent to us by a push Intent from any supported platform.
     * For that reason, it needs to be checked in multiple ways.
     *
     * @param pushBundle a [Bundle], or `null`.
     * @return a [String], or `null`.
     */
    @InternalUseOnly
    fun getApptentivePushNotificationData(pushBundle: Bundle?): String? {
        if (pushBundle != null) {
            when {
                pushBundle.containsKey(PUSH_EXTRA_KEY_PARSE) -> { // Parse
                    Log.v(PUSH_NOTIFICATION, "Got a Parse Push.")
                    val parseDataString = pushBundle.getString(PUSH_EXTRA_KEY_PARSE)
                    if (parseDataString == null) {
                        Log.e(PUSH_NOTIFICATION, "com.parse.Data is null.")
                        return null
                    }
                    return try {
                        val parseJson = JSONObject(parseDataString)
                        parseJson.optString(APPTENTIVE_PUSH_EXTRA_KEY)
                    } catch (e: JSONException) {
                        Log.e(PUSH_NOTIFICATION, "com.parse.Data is corrupt: $parseDataString", e)
                        null
                    }
                }
                pushBundle.containsKey(PUSH_EXTRA_KEY_UA) -> { // Urban Airship
                    Log.v(PUSH_NOTIFICATION, "Got an Urban Airship push.")
                    val uaPushBundle = pushBundle.getBundle(PUSH_EXTRA_KEY_UA)
                    if (uaPushBundle == null) {
                        Log.e(PUSH_NOTIFICATION, "Urban Airship push extras bundle is null")
                        return null
                    }
                    return uaPushBundle.getString(APPTENTIVE_PUSH_EXTRA_KEY)
                }
                pushBundle.containsKey(APPTENTIVE_PUSH_EXTRA_KEY) -> { // All others
                    // Straight FCM / GCM / SNS, or nested
                    Log.v(PUSH_NOTIFICATION, "Found apptentive push data.")
                    return pushBundle.getString(APPTENTIVE_PUSH_EXTRA_KEY)
                }
                else -> {
                    Log.e(PUSH_NOTIFICATION, "Got an unrecognizable push.")
                }
            }
        }
        Log.e(PUSH_NOTIFICATION, "Push bundle was null.")
        return null
    }

    @InternalUseOnly
    fun getApptentivePushNotificationData(pushData: Map<String, String>?): String? {
        return pushData?.get(APPTENTIVE_PUSH_EXTRA_KEY)
    }

    private const val PUSH_CONVERSATION_ID = "conversation_id"
    private const val PUSH_ACTION = "action"

    fun generatePendingIntentFromApptentivePushData(
        context: Context,
        client: ApptentiveDefaultClient,
        apptentivePushData: String?,
    ): PendingIntent? {
        Log.d(PUSH_NOTIFICATION, "Generating Apptentive push PendingIntent.")
        if (!apptentivePushData.isNullOrEmpty()) {
            try {
                val pushJson = JSONObject(apptentivePushData)

                // We need to check if current user is actually the receiver of this notification
                val conversationId = pushJson.optString(PUSH_CONVERSATION_ID)
                if (client.getConversationId() != conversationId) {
                    Log.i(
                        PUSH_NOTIFICATION,
                        "Can't generate pending intent from Apptentive push data: " +
                            "Push conversation id doesn't match active conversation"
                    )
                    return null
                }
                when (
                    val action =
                        if (pushJson.has(PUSH_ACTION)) PushAction.parse(pushJson.getString(PUSH_ACTION))
                        else PushAction.unknown
                ) {
                    PushAction.pmc -> {
                        // Construct a pending intent to launch message center
                        Log.d(
                            PUSH_NOTIFICATION,
                            "Push action for Message Center found. Generating pending intent"
                        )
                        return getPendingMessageCenterNotificationIntent(context)
                    }
                    else -> Log.w(
                        PUSH_NOTIFICATION,
                        "Unknown Apptentive push notification action: ${action.name}"
                    )
                }
            } catch (e: Exception) {
                Log.e(PUSH_NOTIFICATION, "Error parsing JSON from push notification.", e)
            }
        }
        return null
    }

    private enum class PushAction {
        pmc, // Present Message Center.
        unknown;

        companion object {
            // Anything unknown will not be handled.
            fun parse(name: String): PushAction {
                try {
                    return valueOf(name)
                } catch (e: IllegalArgumentException) {
                    Log.e(
                        PUSH_NOTIFICATION,
                        "This version of the SDK can't handle push action '$name'",
                        e
                    )
                }
                return unknown
            }
        }
    }
}
