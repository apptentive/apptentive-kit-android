package apptentive.com.android.feedback.engagement.criteria

import apptentive.com.android.feedback.utils.IndentBufferedPrinter
import apptentive.com.android.util.InternalUseOnly
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.INTERACTIONS

@InternalUseOnly
interface InteractionCriteria {
    fun isMet(state: TargetingState, verbose: Boolean = false): Boolean
}

internal data class InteractionClauseCriteria(private val rootClause: Clause) : InteractionCriteria {
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
