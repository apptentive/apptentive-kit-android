package apptentive.com.android.feedback.engagement.criteria

import apptentive.com.android.core.Converter
import apptentive.com.android.feedback.model.InvocationData

// FIXME: unit tests
object InvocationConverter : Converter<InvocationData, Invocation> {
    override fun convert(source: InvocationData) = Invocation(
        interactionId = source.interactionId,
        criteria = convertInteractionCriteria(source.criteria)
    )

    private fun convertInteractionCriteria(source: Map<String, Any>): InteractionCriteria {
        val rootClause = convertClause(source)
        return InteractionClauseCriteria(rootClause)
    }

    private fun convertClause(source: Map<String, Any>): Clause {
        return convertClause(and, source)
    }

    private fun convertClause(key: String, source: Any): Clause {
        return when (key) {
            and -> LogicalAndClause(convertClauseChildren(source))
            or -> LogicalOrClause(convertClauseChildren(source))
            not -> LogicalNotClause(convertClauseChildren(source))
            else -> {
                val field = Field.parse(key)
                if (field is Field.unknown) {
                    throw IllegalArgumentException("Unknown field: ${field.path}")
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
        val parameter = field.convertValue(value)
        return ConditionalTest(operator, parameter)
    }

    private fun convertClauseChildren(source: Any): List<Clause> {
        return when (source) {
            is List<*> -> {
                source.map { convertClause(and, it as Any) }
            }
            is Map<*, *> -> {
                source.map { convertClause(it.key as String, it.value as Any) }
            }
            else -> throw IllegalArgumentException("Invalid source: $source")
        }
    }

    private const val and = "\$and"
    private const val or = "\$or"
    private const val not = "\$not"
}