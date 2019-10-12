package apptentive.com.android.feedback.engagement.criteria

interface TargetingState {
    fun getValue(fieldPath: FieldPath): Value
}

typealias FieldPath = String
