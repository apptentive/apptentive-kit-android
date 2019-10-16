package apptentive.com.android.feedback.model

data class CustomData(val content: Map<String, Any?> = mapOf()) {
    operator fun get(key: String): Any? = content[key]
}