package apptentive.com.android.feedback.engagement.interactions

import androidx.annotation.VisibleForTesting

/**
 * An object that converts raw interaction data to a concrete interaction class.
 */
@VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
interface InteractionDataConverter {
    fun convert(data: InteractionData): Interaction?
}

data class DefaultInteractionDataConverter(
    private val lookup: Map<String, InteractionTypeConverter<*>>
) : InteractionDataConverter {
    override fun convert(data: InteractionData): Interaction? {
        val converter = lookup[data.type]
        return converter?.convert(data)
    }
}
