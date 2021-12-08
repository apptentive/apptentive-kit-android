package apptentive.com.android.feedback.platform

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import apptentive.com.android.concurrent.Executors
import apptentive.com.android.feedback.Apptentive
import apptentive.com.android.feedback.engagement.Engagement
import apptentive.com.android.feedback.engagement.EngagementContext
import apptentive.com.android.feedback.payload.PayloadSender

/**
 * Android-specific implementation of the [EngagementContext].
 * @param androidContext - enclosing [android.content.Context] object.
 * @param engagement - target [Engagement] object.
 */
class AndroidEngagementContext(
    val androidContext: Context,
    engagement: Engagement,
    payloadSender: PayloadSender,
    executors: Executors
) : EngagementContext(engagement, payloadSender, executors) {
    fun getString(resId: Int): String {
        return androidContext.getString(resId)
    }

    fun tryStartActivity(intent: Intent) = androidContext.tryStartActivity(intent)

    fun getFragmentManager(context: Context? = Apptentive.getApptentiveActivityCallback()?.getApptentiveActivityInfo()): FragmentManager {
        return when (context) {
            is AppCompatActivity, is FragmentActivity -> (context as FragmentActivity).supportFragmentManager
            is ContextThemeWrapper -> getFragmentManager(context.baseContext)
            is Application -> throw Exception(
                "Can't retrieve fragment manager. " +
                    "Must use an Activity context. " +
                    "Application context does not have a fragment manager."
            )
            null -> throw Exception("Context is null")
            else -> throw Exception("Can't retrieve fragment manager. Unknown context type ${context.packageName}")
        }
    }
}
