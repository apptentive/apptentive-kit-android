package apptentive.com.android.feedback.engagement.interactions

import apptentive.com.android.util.InternalUseOnly
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags

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
        return try {
            val converter = lookup[data.type]
            converter?.convert(data)
        } catch (e: Exception) {
            Log.e(LogTags.INTERACTIONS, "Failed to convert interaction data", e)
            null
        }
    }
}
