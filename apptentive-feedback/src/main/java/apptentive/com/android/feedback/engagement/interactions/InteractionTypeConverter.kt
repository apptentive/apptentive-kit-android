package apptentive.com.android.feedback.engagement.interactions

import apptentive.com.android.util.InternalUseOnly

@InternalUseOnly
interface InteractionTypeConverter<out T : Interaction> {
    fun convert(data: InteractionData): T
}
