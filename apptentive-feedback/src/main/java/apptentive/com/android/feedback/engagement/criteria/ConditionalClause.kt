package apptentive.com.android.feedback.engagement.criteria

class ConditionalClause(
    private val query: String,
    private val tests: List<ConditionalTest>
) : Clause {
    private val field: Field by lazy {
        Field.parse(query)
    }

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