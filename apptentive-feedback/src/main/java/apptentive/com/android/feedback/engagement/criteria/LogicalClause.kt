package apptentive.com.android.feedback.engagement.criteria

import apptentive.com.android.feedback.utils.IndentPrinter

internal abstract class LogicalClause(protected val children: List<Clause>) : Clause {
    protected abstract val operator: String

    override fun evaluate(state: TargetingState, printer: IndentPrinter?): Boolean {
        try {
            val shouldPrint = shouldPrint()
            if (shouldPrint) {
                printer?.print("- $operator:")
                printer?.startBlock()
            }
            return evaluateLogicalClause(state, printer)
        } finally {
            if (shouldPrint()) {
                printer?.endBlock()
            }
        }
    }

    protected abstract fun evaluateLogicalClause(state: TargetingState, printer: IndentPrinter?): Boolean
    protected open fun shouldPrint() = true
}

internal class LogicalAndClause(children: List<Clause>) : LogicalClause(children) {
    override val operator: String get() = "and"

    override fun evaluateLogicalClause(state: TargetingState, printer: IndentPrinter?): Boolean {
        return children.all { clause: Clause -> clause.evaluate(state, printer) }
    }

    override fun shouldPrint() = children.size > 1
}

internal class LogicalOrClause(children: List<Clause>) : LogicalClause(children) {
    override val operator: String get() = "or"

    override fun evaluateLogicalClause(state: TargetingState, printer: IndentPrinter?): Boolean {
        return children.any { clause: Clause -> clause.evaluate(state, printer) }
    }
}

internal class LogicalNotClause(children: List<Clause>) : LogicalClause(children) {
    override val operator: String get() = "not"

    init {
        require(children.size == 1) { "Expected 1 child but was ${children.size}" }
    }

    override fun evaluateLogicalClause(state: TargetingState, printer: IndentPrinter?): Boolean {
        return !children.first().evaluate(state, printer)
    }
}
