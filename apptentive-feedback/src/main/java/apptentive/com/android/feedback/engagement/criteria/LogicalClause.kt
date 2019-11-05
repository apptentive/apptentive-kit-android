package apptentive.com.android.feedback.engagement.criteria

abstract class LogicalClause(protected val children: List<Clause>) : Clause

class LogicalAndClause(children: List<Clause>) : LogicalClause(children) {
    override fun evaluate(state: TargetingState): Boolean {
        return children.all { clause: Clause -> clause.evaluate(state) }
    }
}

class LogicalOrClause(children: List<Clause>) : LogicalClause(children) {
    override fun evaluate(state: TargetingState): Boolean {
        return children.any { clause: Clause -> clause.evaluate(state) }
    }
}

class LogicalNotClause(children: List<Clause>) : LogicalClause(children) {
    init {
        require(children.size == 1) { "Expected 1 child but was ${children.size}" }
    }

    override fun evaluate(state: TargetingState): Boolean {
        return !children.first().evaluate(state)
    }
}