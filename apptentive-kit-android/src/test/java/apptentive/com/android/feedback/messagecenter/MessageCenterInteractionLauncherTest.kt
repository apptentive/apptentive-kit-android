package apptentive.com.android.feedback.messagecenter

import apptentive.com.android.TestCase
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.feedback.EngagementResult
import apptentive.com.android.feedback.engagement.EngageArgs
import apptentive.com.android.feedback.engagement.EngagementContextFactory
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.MockEngagementContext
import apptentive.com.android.feedback.engagement.MockEngagementContextFactory
import apptentive.com.android.feedback.interactions.messagecenter.interaction.MessageCenterInteractionLauncher
import apptentive.com.android.feedback.message.MessageCenterInteraction
import org.junit.Test

class MessageCenterInteractionLauncherTest : TestCase() {

    private val launcher = MessageCenterInteractionLauncher()

    @Test
    fun `launchInteraction always passes whereEvent as null`() {
        DependencyProvider.register(
            MockEngagementContextFactory
            {
                MockEngagementContext(
                    onEngage = { args ->
                        addResult(args)
                        EngagementResult.InteractionNotShown("No runnable interactions")
                    },
                )
            }
        )
        try {
            launcher.launchInteraction(
                DependencyProvider.of<EngagementContextFactory>().engagementContext(),
                MessageCenterInteraction(
                    "MC",
                    "Title",
                    "Description",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                ),
                "MC Event"
            )
        } catch (e: Exception) {
        }

        assertResults(
            EngageArgs(
                Event("com.apptentive", "MessageCenter", "launch"),
                "MC",
                whereEvent = null
            )
        )
    }
}
