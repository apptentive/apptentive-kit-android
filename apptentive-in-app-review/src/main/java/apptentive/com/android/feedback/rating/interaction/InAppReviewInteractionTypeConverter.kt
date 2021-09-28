package apptentive.com.android.feedback.rating.interaction

import apptentive.com.android.feedback.engagement.interactions.InteractionData
import apptentive.com.android.feedback.engagement.interactions.InteractionTypeConverter

internal class InAppReviewInteractionTypeConverter : InteractionTypeConverter<InAppReviewInteraction>  {
    override fun convert(data: InteractionData) = InAppReviewInteraction(
        id = data.id
    )
}
