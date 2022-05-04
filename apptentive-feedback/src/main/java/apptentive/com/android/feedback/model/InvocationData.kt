package apptentive.com.android.feedback.model

import apptentive.com.android.util.InternalUseOnly

@InternalUseOnly
data class InvocationData(val interactionId: String, val criteria: Map<String, Any> = emptyMap())
