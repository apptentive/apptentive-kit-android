package apptentive.com.android.feedback.messagecenter.interaction

import android.os.Handler
import android.os.Looper
import androidx.annotation.Keep
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.feedback.dependencyprovider.MessageCenterModelProvider
import apptentive.com.android.feedback.engagement.EngagementContext
import apptentive.com.android.feedback.message.MessageCenterInteraction
import apptentive.com.android.feedback.messagecenter.view.MessageCenterActivity
import apptentive.com.android.feedback.platform.AndroidViewInteractionLauncher
import apptentive.com.android.feedback.utils.saveInteractionBackup
import apptentive.com.android.ui.startViewModelActivity
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.INTERACTIONS

@Keep
internal class MessageCenterInteractionLauncher : AndroidViewInteractionLauncher<MessageCenterInteraction>() {
    override fun launchInteraction(
        engagementContext: EngagementContext,
        interaction: MessageCenterInteraction
    ) {
        super.launchInteraction(engagementContext, interaction)

        Log.i(INTERACTIONS, "Message Center interaction launched with title: ${interaction.title}")
        Log.v(INTERACTIONS, "Message Center interaction data: $interaction")

        saveInteractionBackup(interaction)

        DependencyProvider.register(MessageCenterModelProvider(interaction))

        // Launch Message center landing page
        launchMessageCenterWithARetry(engagementContext, interaction, 1)
    }

    private fun launchMessageCenterWithARetry(engagementContext: EngagementContext, interaction: MessageCenterInteraction, retryCount: Int) {
        engagementContext.executors.main.execute {
            try {
                engagementContext.getAppActivity().startViewModelActivity<MessageCenterActivity>()
            } catch (e: Exception) {
                if (retryCount > 0) {
                    engagementContext.executors.state.execute {
                        Log.e(INTERACTIONS, "Could not start Message Center interaction retrying in 1 second", e)
                        Handler(Looper.getMainLooper()).postDelayed({
                            launchMessageCenterWithARetry(engagementContext, interaction, retryCount - 1)
                        }, 1000)
                    }
                } else {
                    Log.e(INTERACTIONS, "Could not start Message Center interaction after a retry", e)
                }
            }
        }
    }
}
