package apptentive.com.android.feedback.engagement

import android.content.Context
import apptentive.com.android.feedback.EngagementResult
import apptentive.com.android.feedback.engagement.interactions.Interaction
import apptentive.com.android.feedback.engagement.interactions.InteractionLauncher

interface InteractionEngagement {
    fun engage(context: Context, interaction: Interaction): EngagementResult
}

data class DefaultInteractionEngagement(
    private val lookup: Map<Class<Interaction>, InteractionLauncher<Interaction>>
) : InteractionEngagement {
    @Suppress("UNCHECKED_CAST")
    override fun engage(context: Context, interaction: Interaction): EngagementResult {
        val interactionClass = interaction.javaClass
        val launcher = lookup[interactionClass]
        if (launcher != null) {
            launcher.launchInteraction(context, interaction)
            return EngagementResult.Success
        }

        return EngagementResult.Error("Interaction launcher not found: ${interactionClass.name}") // TODO: better error description
    }
}
