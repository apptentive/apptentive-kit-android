package apptentive.com.android.feedback.engagement

import apptentive.com.android.feedback.EngagementResult
import apptentive.com.android.feedback.engagement.interactions.Interaction
import apptentive.com.android.feedback.engagement.interactions.InteractionLauncher
import apptentive.com.android.feedback.utils.ThrottleUtils
import apptentive.com.android.util.InternalUseOnly

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

        return if (launcher != null) {
            val shouldThrottleInteraction = ThrottleUtils.shouldThrottleInteraction(interaction)

            if (!shouldThrottleInteraction) {
                launcher.launchInteraction(context, interaction)
                EngagementResult.InteractionShown(interactionId = interaction.id)
            } else EngagementResult.InteractionNotShown("${interaction.type.name} throttled.")
        } else EngagementResult.Error("Interaction launcher not found: ${interactionClass.name}")
    }
}
