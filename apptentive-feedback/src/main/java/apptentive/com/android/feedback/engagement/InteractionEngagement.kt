package apptentive.com.android.feedback.engagement

import androidx.annotation.VisibleForTesting
import apptentive.com.android.feedback.EngagementResult
import apptentive.com.android.feedback.engagement.interactions.Interaction
import apptentive.com.android.feedback.engagement.interactions.InteractionLauncher
import apptentive.com.android.feedback.utils.ThrottleUtils

@VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
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
                EngagementResult.Success(interactionId = interaction.id)
            } else EngagementResult.Failure("${interaction.type.name} throttled.")
        } else EngagementResult.Error("Interaction launcher not found: ${interactionClass.name}")
    }
}
