package apptentive.com.android.feedback.engagement.criteria

interface InteractionCriteria {
    fun isMet(state: TargetingState): Boolean // TODO: return a result with more context info
}

data class InteractionClauseCriteria(private val rootClause: Clause) : InteractionCriteria {
    override fun isMet(state: TargetingState) = rootClause.evaluate(state)
}