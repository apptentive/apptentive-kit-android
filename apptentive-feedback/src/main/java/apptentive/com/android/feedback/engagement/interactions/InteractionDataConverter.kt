package apptentive.com.android.feedback.engagement.interactions

/**
 * An object that converts raw interaction data to a concrete interaction class.
 */
interface InteractionDataConverter {
    fun convert(data: InteractionData): Interaction?
}

typealias InteractionType = String

data class DefaultInteractionDataConverter(
    private val lookup: Map<InteractionType, InteractionTypeConverter<*>>
) : InteractionDataConverter {
    override fun convert(data: InteractionData): Interaction? {
        val converter = lookup[data.type]
        return converter?.convert(data)
    }
}