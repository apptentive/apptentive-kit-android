package apptentive.com.android.feedback.engagement.interactions

import apptentive.com.android.core.LogTags
import apptentive.com.android.util.Log
/**
 * An object that converts raw interaction data to a concrete interaction class.
 */
internal interface InteractionDataConverter {
    fun convert(data: InteractionData): Interaction?
}

internal data class DefaultInteractionDataConverter(
    private val lookup: Map<String, InteractionTypeConverter<*>>
) : InteractionDataConverter {
    override fun convert(data: InteractionData): Interaction? {
        return try {
            val converter = lookup[data.type]
            converter?.convert(data)
        } catch (e: Exception) {
            Log.e(LogTags.INTERACTIONS, "Failed to convert interaction data", e)
            null
        }
    }
}
