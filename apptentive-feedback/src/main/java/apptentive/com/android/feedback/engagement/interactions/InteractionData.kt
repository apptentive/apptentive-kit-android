package apptentive.com.android.feedback.engagement.interactions

// TODO: exclude this class from ProGuard
data class InteractionData(
    val id: String,
    val type: String,
    val displayType: String? = null,
    val configuration: Map<String, *>
)