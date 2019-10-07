package apptentive.com.android.feedback.engagement.criteria

abstract class LogicalClause(protected val children: List<Clause>) : Clause

class LogicalAndClause(children: List<Clause>) : LogicalClause(children) {
    override fun evaluate(state: TargetingState): Boolean {
        for (clause in children) {
            val ret = clause.evaluate(state)
            if (!ret) {
                return false
            }
        }
        return true
    }
}

class LogicalOrClause(children: List<Clause>) : LogicalClause(children) {
    override fun evaluate(state: TargetingState): Boolean {
        for (clause in children) {
            val ret = clause.evaluate(state)
            if (ret) {
                return true
            }
        }
        return false
    }
}

class LogicalNotClause(children: List<Clause>) : LogicalClause(children) {
    init {
        require(children.size == 1) { "Expected 1 child but was ${children.size}" }
    }

    override fun evaluate(state: TargetingState): Boolean {
        val clause = children[0]
        val ret = clause.evaluate(state)
        return !ret
    }
}