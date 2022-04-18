package apptentive.com.android.feedback.engagement

import android.app.Activity
import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import apptentive.com.android.concurrent.Executors
import apptentive.com.android.feedback.Apptentive
import apptentive.com.android.feedback.engagement.criteria.InvocationConverter
import apptentive.com.android.feedback.engagement.interactions.InteractionResponse
import apptentive.com.android.feedback.model.InvocationData
import apptentive.com.android.feedback.model.payloads.ExtendedData
import apptentive.com.android.feedback.model.payloads.Payload
import apptentive.com.android.feedback.payload.PayloadSender
import apptentive.com.android.util.InternalUseOnly

/**
 * Wrapper class around [Engagement] object.
 * Allows capturing platform specific context (e.g. [android.content.Context]) before making an
 * actual engagement call.
 */
@InternalUseOnly
open class EngagementContext(
    private val engagement: Engagement,
    private val payloadSender: PayloadSender,
    val executors: Executors
) {
    fun engage(
        event: Event,
        interactionId: String? = null,
        data: Map<String, Any?>? = null,
        customData: Map<String, Any?>? = null,
        extendedData: List<ExtendedData>? = null,
        interactionResponses: Map<String, Set<InteractionResponse>>? = null
    ) = engagement.engage(
        context = this,
        event = event,
        interactionId = interactionId,
        data = data,
        customData = customData,
        extendedData = extendedData,
        interactionResponses = interactionResponses
    )

    fun engage(invocations: List<InvocationData>) = engagement.engage(
        context = this,
        invocations = invocations.map(InvocationConverter::convert)
    )

    fun sendPayload(payload: Payload) = payloadSender.sendPayload(payload)

    @VisibleForTesting
    fun getEngagement() = engagement

    @VisibleForTesting
    fun getPayloadSender() = payloadSender

    fun getFragmentManager(context: Context? = getActivityInfo()): FragmentManager {
        return when (context) {
            is AppCompatActivity, is FragmentActivity -> (context as FragmentActivity).supportFragmentManager
            is ContextThemeWrapper -> getFragmentManager(context.baseContext)
            null -> throw Exception("Context is null")
            else -> throw Exception("Can't retrieve fragment manager. Unknown context type ${context.packageName}")
        }
    }

    fun getActivityContext(): Context {
        return requireNotNull(getActivityInfo()) {
            "Apptentive Activity Callback not registered. " +
                "Extend ApptentiveActivity.kt, implement the getActivity() function, " +
                "and call registerApptentiveActivityCallback(this) " +
                "in your Activity's onCreate function."
        }
    }

    private fun getActivityInfo(): Activity? =
        Apptentive.getApptentiveActivityCallback()?.getApptentiveActivityInfo()
}
