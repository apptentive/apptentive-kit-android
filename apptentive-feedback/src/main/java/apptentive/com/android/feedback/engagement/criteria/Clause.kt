package apptentive.com.android.feedback.engagement.criteria

interface Clause {
    fun evaluate(state: TargetingState): Boolean
}