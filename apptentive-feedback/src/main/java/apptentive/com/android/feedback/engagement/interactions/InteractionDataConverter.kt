package apptentive.com.android.feedback.engagement.interactions

import apptentive.com.android.util.InternalUseOnly

/**
 * An object that converts raw interaction data to a concrete interaction class.
 */
@InternalUseOnly
interface InteractionDataConverter {
    fun convert(data: InteractionData): Interaction?
}

internal data class DefaultInteractionDataConverter(
    private val lookup: Map<String, InteractionTypeConverter<*>>
) : InteractionDataConverter {
    override fun convert(data: InteractionData): Interaction? {
        val converter = lookup[data.type]
        return converter?.convert(data)
    }
}
