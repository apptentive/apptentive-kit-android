package apptentive.com.android.feedback.engagement.interactions

data class InteractionData(
    val id: String,
    val type: String,
    val display_type: String? = null,
    val configuration: Map<String, *>
)