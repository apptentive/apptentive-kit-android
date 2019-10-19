package apptentive.com.android.feedback.model

data class CustomData(val content: Map<String, Any?> = emptyMap()) {
    operator fun get(key: String): Any? = content[key]
}