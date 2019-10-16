package apptentive.com.android.feedback.engagement.criteria

import apptentive.com.android.core.Converter
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.model.TargetData

data class Target(val interactionId: String, val criteria: InteractionCriteria)

data class Targets(val targets: Map<Event, List<Target>> = mapOf()) {
    operator fun get(event: Event) = targets[event]
}

class ClauseConverter : Converter<Map<String, Any>, Clause> {
    override fun convert(source: Map<String, Any>): Clause {
        return convert(and, source)
    }

    private fun convert(key: String, source: Any): Clause {
        return when (key) {
            and -> LogicalAndClause(convertChildren(source))
            or -> LogicalOrClause(convertChildren(source))
            not -> LogicalNotClause(convertChildren(source))
            else -> {
                val field = Field.parse(key)
                if (field is Field.unknown) {
                    TODO() // FIXME: return special clause which would fail criteria
                } else {
                    ConditionalClause(field, convertConditionalTests(field, source))
                }
            }
        }
    }

    private fun convertConditionalTests(
        field: Field,
        source: Any
    ): List<ConditionalTest> {
        return when (source) {
            is Map<*, *> -> {
                return source.map { (key, value) ->
                    convertConditionalTest(
                        field = field,
                        key = key as String,
                        value = value
                    )
                }
            }
            else -> TODO()
        }
    }

    private fun convertConditionalTest(field: Field, key: String, value: Any?): ConditionalTest {
        val operator = ConditionalOperator.parse(key)
        val parameter = field.convert(value)
        return ConditionalTest(operator, parameter)
    }

    private fun convertComplexValue(field: Field, source: Map<*, *>): List<ConditionalTest> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun convertChildren(source: Any): List<Clause> {
        when (source) {
            is List<*> -> {
                return source.map { convert(and, it as Any) }
            }
            is Map<*, *> -> {
                return source.map { convert(it.key as String, it.value as Any) }
            }
            else -> throw IllegalArgumentException("Invalid source: $source")
        }
    }

    companion object {
        private const val and = "\$and"
        private const val or = "\$or"
        private const val not = "\$not"
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