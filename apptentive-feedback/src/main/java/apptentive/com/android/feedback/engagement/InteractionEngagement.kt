package apptentive.com.android.feedback.engagement

import apptentive.com.android.feedback.EngagementResult
import apptentive.com.android.feedback.engagement.interactions.Interaction
import apptentive.com.android.feedback.engagement.interactions.InteractionLauncher

interface InteractionEngagement {
    fun engage(interaction: Interaction): EngagementResult
}

data class DefaultInteractionEngagement(
    private val lookup: Map<Class<*>, InteractionLauncher<*>>
) : InteractionEngagement {
    @Suppress("UNCHECKED_CAST")
    override fun engage(interaction: Interaction): EngagementResult {
        val interactionClass = interaction.javaClass
        val launcher = lookup[interactionClass] as? InteractionLauncher<Interaction>
        if (launcher != null) {
            launcher.launchInteraction(interaction)
            return EngagementResult.Success
        }

        return EngagementResult.Error("Interaction launcher not found: ${interactionClass.name}")
    }
}
