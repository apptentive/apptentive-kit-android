package apptentive.com.android.feedback.engagement.criteria

class ConditionalClause(
    private val fieldPath: FieldPath,
    private val tests: List<ConditionalTest>
) : Clause {
    override fun evaluate(state: TargetingState): Boolean {
        val value = state.getValue(fieldPath)
        for (test in tests) {
            val result = test.operator.apply(value, test.parameter)
            if (!result) {
                return false
            }
        }
        return true
    }
}