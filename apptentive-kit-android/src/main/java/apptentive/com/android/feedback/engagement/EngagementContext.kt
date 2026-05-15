package apptentive.com.android.feedback.engagement

import android.app.Activity
import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import apptentive.com.android.core.concurrent.Executors
import apptentive.com.android.feedback.Apptentive
import apptentive.com.android.feedback.engagement.criteria.InvocationConverter
import apptentive.com.android.feedback.engagement.interactions.InteractionResponse
import apptentive.com.android.feedback.model.InvocationData
import apptentive.com.android.feedback.model.payloads.ExtendedData
import apptentive.com.android.feedback.model.payloads.Payload
import apptentive.com.android.feedback.payload.PayloadSender
import apptentive.com.android.feedback.platform.ApptentiveKitSDKState.getConversationCredentialProvider
import apptentive.com.android.feedback.utils.ThrottleUtils

/**
 * Wrapper class around [Engagement] object.
 * Allows capturing platform specific context (e.g. [android.content.Context]) before making an
 * actual engagement call.
 */
internal open class EngagementContext(
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
        interactionResponses: Map<String, Set<InteractionResponse>>? = null,
        whereEvent: String? = null
    ) = engagement.engage(
        context = this,
        event = event,
        interactionId = interactionId,
        data = data,
        customData = customData,
        extendedData = extendedData,
        interactionResponses = interactionResponses,
        whereEvent = whereEvent,
    )

    fun engage(invocations: List<InvocationData>, whereEvent: String?) = engagement.engage(
        context = this,
        invocations = invocations.map(InvocationConverter::convert),
        whereEvent = whereEvent
    )

    fun engageToRecordCurrentAnswer(interactionResponses: Map<String, Set<InteractionResponse>>, reset: Boolean = false) {
        engagement.engageToRecordCurrentAnswer(interactionResponses, reset)
    }

    fun getNextQuestionSet(invocations: List<InvocationData>) = engagement.getNextQuestionSet(
        invocations = invocations.map(InvocationConverter::convert)
    )

    fun enqueuePayload(payload: Payload) {
        if (!ThrottleUtils.sdkEnabled) return
        val conversationCredential = getConversationCredentialProvider()
        payloadSender.enqueuePayload(payload, conversationCredential)
    }

    @VisibleForTesting
    fun getEngagement() = engagement

    @VisibleForTesting
    fun getPayloadSender() = payloadSender

    fun getFragmentManager(context: Context? = getAppActivity()): FragmentManager {
        return when (context) {
            is AppCompatActivity, is FragmentActivity -> (context as FragmentActivity).supportFragmentManager
            is ContextThemeWrapper -> getFragmentManager(context.baseContext)
            null -> throw Exception("Context is null")
            else -> throw Exception("Can't retrieve fragment manager. Unknown context type ${context.packageName}")
        }
    }

    fun getAppActivity(): Activity {
        return requireNotNull(getAppActivityInfo()) {
            "Apptentive Activity Callback not registered. " +
                "Extend ApptentiveActivityInfo.kt, " +
                "implement the getApptentiveActivityInfo() function, " +
                "and call registerApptentiveActivityInfoCallback(this) " +
                "in your Activity's onResume function."
        }
    }

    private fun getAppActivityInfo(): Activity? =
        Apptentive.getApptentiveActivityCallback()?.getApptentiveActivityInfo()
}
