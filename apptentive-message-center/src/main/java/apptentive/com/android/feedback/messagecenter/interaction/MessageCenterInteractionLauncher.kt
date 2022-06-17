package apptentive.com.android.feedback.messagecenter.interaction

import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.feedback.dependencyprovider.MessageCenterModelProvider
import apptentive.com.android.feedback.engagement.EngagementContext
import apptentive.com.android.feedback.message.MessageCenterInteraction
import apptentive.com.android.feedback.messagecenter.view.MessageCenterActivity
import apptentive.com.android.feedback.platform.AndroidViewInteractionLauncher
import apptentive.com.android.ui.startViewModelActivity
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

        DependencyProvider.register(MessageCenterModelProvider(interaction))

        // Launch Message center landing page
        engagementContext.executors.main.execute {
            engagementContext.getAppActivity().startViewModelActivity<MessageCenterActivity>()
        }
    }
}
