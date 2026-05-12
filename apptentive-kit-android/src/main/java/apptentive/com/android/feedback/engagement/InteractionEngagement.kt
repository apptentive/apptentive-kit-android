package apptentive.com.android.feedback.engagement

import apptentive.com.android.core.util.InternalUseOnly
import apptentive.com.android.core.util.Log
import apptentive.com.android.core.util.LogTags.FEEDBACK
import apptentive.com.android.feedback.EngagementResult
import apptentive.com.android.feedback.engagement.interactions.Interaction
import apptentive.com.android.feedback.engagement.interactions.InteractionLauncher

@InternalUseOnly
interface InteractionEngagement {
    fun engage(context: EngagementContext, interaction: Interaction, whereEvent: String?): EngagementResult
}

internal data class DefaultInteractionEngagement(
    private val lookup: Map<Class<Interaction>, InteractionLauncher<Interaction>>
) : InteractionEngagement {
    @Suppress("UNCHECKED_CAST")
    override fun engage(context: EngagementContext, interaction: Interaction, whereEvent: String?): EngagementResult {
        val interactionClass = interaction.javaClass
        val launcher = lookup[interactionClass]
        return try {
            if (launcher != null) {
                launcher.launchInteraction(context, interaction, whereEvent)
                EngagementResult.InteractionShown(interactionId = interaction.id)
            } else EngagementResult.Error("Interaction launcher not found: ${interactionClass.name}")
        } catch (exception: Exception) {
            Log.e(FEEDBACK, "Cannot show Interaction", exception)
            EngagementResult.Error("Cannot show Interaction: ${interactionClass.name}")
        }
    }
}
