package apptentive.com.android.feedback.notifications

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import apptentive.com.android.feedback.Apptentive
import apptentive.com.android.feedback.ApptentiveActivityInfo
import apptentive.com.android.feedback.EngagementResult
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.MESSAGE_CENTER
import apptentive.com.android.util.LogTags.PUSH_NOTIFICATION

class ApptentiveNotificationActivity : AppCompatActivity(), ApptentiveActivityInfo {

    override fun onResume() {
        super.onResume()
        Apptentive.registerApptentiveActivityInfoCallback(this)
        handleEventIntent()
    }

    private fun handleEventIntent() {
        val eventExtra = intent.getStringExtra(APPTENTIVE_NOTIFICATION_EVENT)
        Log.d(PUSH_NOTIFICATION, "Event extra from push intent: $eventExtra")

        when (eventExtra) {
            APPTENTIVE_NOTIFICATION_MESSAGE_CENTER -> {
                Apptentive.showMessageCenter {
                    if (it is EngagementResult.InteractionShown) {
                        Log.i(MESSAGE_CENTER, "Message Center shown from Notification")
                        finish()
                    } else {
                        Log.e(MESSAGE_CENTER, "Error showing Message Center")
                        finish()
                    }
                }
            }
            else -> {
                Log.e(PUSH_NOTIFICATION, "Unknown event type: $eventExtra")
                finish()
            }
        }
    }

    override fun getApptentiveActivityInfo(): Activity {
        return this
    }

    override fun onPause() {
        Apptentive.unregisterApptentiveActivityInfoCallback()
        super.onPause()
    }

    companion object {
        private const val APPTENTIVE_NOTIFICATION = "notification"
        const val APPTENTIVE_NOTIFICATION_EVENT = "${APPTENTIVE_NOTIFICATION}_event"
        const val APPTENTIVE_NOTIFICATION_MESSAGE_CENTER = "${APPTENTIVE_NOTIFICATION}_message_center"
    }
}
