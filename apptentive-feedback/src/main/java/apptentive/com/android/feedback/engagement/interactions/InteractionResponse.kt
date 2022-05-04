package apptentive.com.android.feedback.engagement.interactions

import apptentive.com.android.util.InternalUseOnly

@InternalUseOnly
sealed class InteractionResponse {
    data class IdResponse(val id: String) : InteractionResponse() // Notes / Single Choice / Multiple Choice
    data class LongResponse(val response: Long) : InteractionResponse() // Range
    data class StringResponse(val response: String) : InteractionResponse() // FreeForm
    data class OtherResponse(val id: String?, val response: String?) : InteractionResponse() // Multi Other / Single Other
}
