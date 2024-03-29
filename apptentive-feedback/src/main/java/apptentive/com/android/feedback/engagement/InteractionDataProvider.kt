package apptentive.com.android.feedback.engagement

import apptentive.com.android.feedback.engagement.criteria.Invocation
import apptentive.com.android.feedback.engagement.interactions.InteractionData
import apptentive.com.android.util.InternalUseOnly

@InternalUseOnly
interface InteractionDataProvider {
    fun getInteractionData(event: Event): InteractionData?
    fun getInteractionData(invocations: List<Invocation>): InteractionData?

    fun getQuestionId(invocations: List<Invocation>): String?
}
