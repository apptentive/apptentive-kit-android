package apptentive.com.android.feedback.engagement

import androidx.annotation.VisibleForTesting
import apptentive.com.android.feedback.engagement.criteria.Invocation
import apptentive.com.android.feedback.engagement.interactions.InteractionData

@VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
interface InteractionDataProvider {
    fun getInteractionData(event: Event): InteractionData?
    fun getInteractionData(invocations: List<Invocation>): InteractionData?
}
