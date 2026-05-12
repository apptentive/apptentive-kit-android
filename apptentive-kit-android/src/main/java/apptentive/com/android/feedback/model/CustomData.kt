package apptentive.com.android.feedback.model

import apptentive.com.android.core.util.InternalUseOnly

@InternalUseOnly
data class CustomData(val content: Map<String, Any?> = emptyMap()) {
    operator fun get(key: String): Any? = content[key]
}
