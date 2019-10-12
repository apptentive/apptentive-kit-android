package apptentive.com.android.feedback.engagement.criteria

import apptentive.com.android.core.Converter
import apptentive.com.android.feedback.model.TargetData

data class Target(val interactionId: String, val criteria: InteractionCriteria)

data class Targets(val targets: Map<String, List<Target>> = mapOf())

class ClauseConverter : Converter<Map<String, Any>, Clause> {
    override fun convert(source: Map<String, Any>): Clause {
        return convert("\$and", source)
    }

    private fun convert(key: String, source: Map<String, Any>): Clause {
        return when (key) {
            "\$and" -> LogicalAndClause(convertChildren(source))
            "\$or" -> LogicalOrClause(convertChildren(source))
            "\$not" -> LogicalNotClause(convertChildren(source))
            else -> ConditionalClause(key, convertConditionalTests(source))
        }
    }

    private fun convertConditionalTests(source: Map<String, Any?>): List<ConditionalTest> {
        return source.map { (key, value) -> convertConditionalTest(key, value) }
    }

    private fun convertConditionalTest(key: String, value: Any?): ConditionalTest {
        val operator = ConditionalOperator.parse(key)
        val parameter = TODO()
        return ConditionalTest(operator, parameter)
    }

    private fun convertChildren(source: Map<String, Any>): List<Clause> {
        return source.map { (key, value) -> convert(key, value as Map<String, Any>)  }
    }
}

class CriteriaConverter(
    private val clauseConverter: Converter<Map<String, Any>, Clause> = ClauseConverter()
) : Converter<Map<String, Any>, InteractionCriteria> {
    override fun convert(source: Map<String, Any>): InteractionCriteria {
        val rootClause = clauseConverter.convert(source)
        return InteractionClauseCriteria(rootClause)
    }
}

data class TargetConverter(
    private val criteriaConverter: Converter<Map<String, Any>, InteractionCriteria> = CriteriaConverter()
) : Converter<TargetData, Target> {
    override fun convert(source: TargetData) = Target(
        interactionId = source.interactionId,
        criteria = criteriaConverter.convert(source.criteria)
    )
}