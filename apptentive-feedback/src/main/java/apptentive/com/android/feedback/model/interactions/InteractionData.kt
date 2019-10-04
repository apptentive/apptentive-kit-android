package apptentive.com.android.feedback.model.interactions

data class InteractionData(
    val id: String,
    val type: String,
    val displayType: String?,
    val configuration: Map<String, *>
)