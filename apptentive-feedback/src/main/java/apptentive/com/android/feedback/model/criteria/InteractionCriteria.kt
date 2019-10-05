package apptentive.com.android.feedback.model.criteria

interface InteractionCriteria {
    fun isMet(state: TargetingState): Boolean
}

data class InteractionClauseCriteria(private val rootClause: Clause) : InteractionCriteria {
    override fun isMet(state: TargetingState) = rootClause.evaluate(state)
}