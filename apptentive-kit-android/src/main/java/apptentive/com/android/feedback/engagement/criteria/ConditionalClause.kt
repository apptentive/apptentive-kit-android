package apptentive.com.android.feedback.engagement.criteria

import apptentive.com.android.feedback.utils.IndentPrinter

internal class ConditionalClause(
    private val field: Field,
    private val tests: List<ConditionalTest>
) : Clause {
    override fun evaluate(state: TargetingState, printer: IndentPrinter?): Boolean {
        val value = state.getValue(field)
        for (test in tests) {
            val result = test.operator.apply(value, test.parameter)
            printer?.print("- ${test.operator.description(field.description, value, test.parameter)} => $result")
            if (!result) {
                return false
            }
        }
        return true
    }
}
