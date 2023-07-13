package apptentive.com.android.feedback.model

import apptentive.com.android.util.InternalUseOnly

/**
 * Data container with the criteria to be evaluated.
 *
 * @param interactionId the id if the interaction or question to be shown
 * @param criteria map of key and values. The keys are expected to predefined Fields in the DefaultTargetingState
 */

@InternalUseOnly
data class InvocationData(
    val interactionId: String,
    val criteria: Map<String, Any> = emptyMap(),
)
