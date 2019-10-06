package apptentive.com.android.feedback.engagement.interactions

interface InteractionFactory {
    fun createInteraction(data: InteractionData): Interaction?
}

typealias InteractionType = String

data class DefaultInteractionFactory(
    private val lookup: Map<InteractionType, InteractionConverter<*>>
) : InteractionFactory {
    override fun createInteraction(data: InteractionData): Interaction? {
        val converter = lookup[data.type]
        return converter?.convert(data)
    }
}