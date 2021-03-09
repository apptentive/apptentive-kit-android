package apptentive.com.android.feedback.engagement.criteria

import apptentive.com.android.feedback.INTERACTIONS
import apptentive.com.android.feedback.utils.IndentBufferedPrinter
import apptentive.com.android.util.Log

interface InteractionCriteria {
    fun isMet(state: TargetingState, verbose: Boolean = false): Boolean
}

data class InteractionClauseCriteria(private val rootClause: Clause) : InteractionCriteria {
    override fun isMet(state: TargetingState, verbose: Boolean): Boolean {
        val printer = if (verbose) IndentBufferedPrinter() else null
        val result = rootClause.evaluate(state, printer)
        Log.i(INTERACTIONS, "Criteria evaluated => $result")
        if (verbose) {
            Log.d(INTERACTIONS, "Criteria evaluation details:\n$printer")
        }
        return result
    }
}