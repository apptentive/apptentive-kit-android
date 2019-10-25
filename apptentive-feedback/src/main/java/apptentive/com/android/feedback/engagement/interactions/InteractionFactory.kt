package apptentive.com.android.feedback.engagement.interactions

// TODO: figure out a better name
interface InteractionFactory {
    fun createInteraction(data: InteractionData): Interaction?
}

typealias InteractionType = String

data class DefaultInteractionFactory(
    private val lookup: Map<InteractionType, InteractionTypeConverter<*>>
) : InteractionFactory {
    override fun createInteraction(data: InteractionData): Interaction? {
        val converter = lookup[data.type]
        return converter?.convert(data)
    }
}