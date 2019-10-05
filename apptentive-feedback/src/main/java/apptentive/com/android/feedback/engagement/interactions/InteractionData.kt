package apptentive.com.android.feedback.engagement.interactions

data class InteractionData(
    val id: String,
    val type: String,
    val display_type: String?,
    val configuration: Map<String, *>
)