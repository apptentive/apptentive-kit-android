package apptentive.com.android.feedback.engagement

import apptentive.com.android.feedback.EngagementResult
import apptentive.com.android.feedback.engagement.interactions.Interaction
import apptentive.com.android.feedback.engagement.interactions.InteractionLauncher
import apptentive.com.android.util.InternalUseOnly
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.FEEDBACK

@InternalUseOnly
interface InteractionEngagement {
    fun engage(context: EngagementContext, interaction: Interaction): EngagementResult
}

internal data class DefaultInteractionEngagement(
    private val lookup: Map<Class<Interaction>, InteractionLauncher<Interaction>>
) : InteractionEngagement {
    @Suppress("UNCHECKED_CAST")
    override fun engage(context: EngagementContext, interaction: Interaction): EngagementResult {
        val interactionClass = interaction.javaClass
        val launcher = lookup[interactionClass]
        return try {
            if (launcher != null) {
                launcher.launchInteraction(context, interaction)
                EngagementResult.InteractionShown(interactionId = interaction.id)
            } else EngagementResult.Error("Interaction launcher not found: ${interactionClass.name}")
        } catch (exception: Exception) {
            Log.e(FEEDBACK, "Cannot show Interaction", exception)
            EngagementResult.Error("Cannot show Interaction: ${interactionClass.name}")
        }
    }
}
