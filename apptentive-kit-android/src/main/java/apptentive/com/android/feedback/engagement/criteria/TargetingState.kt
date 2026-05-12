package apptentive.com.android.feedback.engagement.criteria

import apptentive.com.android.core.util.InternalUseOnly

@InternalUseOnly
interface TargetingState {
    fun getValue(field: Field): Any?
}
