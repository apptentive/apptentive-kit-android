package apptentive.com.android.feedback.engagement.criteria

interface TargetingState {
    fun getValue(fieldPath: FieldPath): Value
}

typealias FieldPath = String

val Value.isNull get() = this is Value.Null