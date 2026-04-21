package apptentive.com

import apptentive.com.android.TestCase
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.feedback.EngagementResult
import apptentive.com.android.feedback.engagement.EngageArgs
import apptentive.com.android.feedback.engagement.EngagementContextFactory
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.MockEngagementContext
import apptentive.com.android.feedback.engagement.MockEngagementContextFactory
import apptentive.com.android.feedback.initiator.InitiatorInteraction
import apptentive.com.android.feedback.initiator.InitiatorInteractionLauncher
import org.junit.Test

class InitiatorInteractionLauncherTest : TestCase() {
    private val launcher = InitiatorInteractionLauncher()

    @Test
    fun `launchInteraction always passes null whereEvent regardless of input`() {
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
        launcher.launchInteraction(
            DependencyProvider.of<EngagementContextFactory>().engagementContext(),
            InitiatorInteraction("Initiator"), "Non-null where event"
        )

        assertResults(EngageArgs(Event("com.apptentive", "Initiator", "launch"), "Initiator"))
    }
}
