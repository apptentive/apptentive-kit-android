package apptentive.com.android.feedback.model

// TODO: exclude this class from ProGuard
data class TargetData(val interactionId: String, val criteria: Map<String, Any> = emptyMap())