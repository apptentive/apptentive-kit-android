package apptentive.com.android.feedback.engagement.interactions

import apptentive.com.android.util.InternalUseOnly

@InternalUseOnly
data class InteractionData(
    val id: String,
    val type: String,
    val displayType: String? = null,
    val configuration: Map<String, *> = mapOf<String, Any?>()
)
