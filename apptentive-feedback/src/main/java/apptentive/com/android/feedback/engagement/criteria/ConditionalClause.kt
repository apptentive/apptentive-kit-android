package apptentive.com.android.feedback.engagement.criteria

class ConditionalClause(
    private val field: Field,
    private val tests: List<ConditionalTest>
) : Clause {
    override fun evaluate(state: TargetingState): Boolean {
        val value = state.getValue(field)
        for (test in tests) {
            val result = test.operator.apply(value, test.parameter)
            if (!result) {
                return false
            }
        }
        return true
    }
}