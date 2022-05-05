package apptentive.com.android.feedback.messagecenter.interaction

import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.feedback.engagement.EngagementContext
import apptentive.com.android.feedback.messagecenter.MessageCenterModelProvider
import apptentive.com.android.feedback.platform.AndroidViewInteractionLauncher
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags

@VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
@Keep
internal class MessageCenterInteractionLauncher : AndroidViewInteractionLauncher<MessageCenterInteraction>() {
    override fun launchInteraction(
        engagementContext: EngagementContext,
        interaction: MessageCenterInteraction
    ) {
        super.launchInteraction(engagementContext, interaction)

        Log.i(LogTags.INTERACTIONS, "Message center interaction launched with title: ${interaction.title}")
        Log.v(LogTags.INTERACTIONS, "Message center interaction data: $interaction")

        // Launch Message center landing page
        DependencyProvider.register(MessageCenterModelProvider(interaction))
    }
}
