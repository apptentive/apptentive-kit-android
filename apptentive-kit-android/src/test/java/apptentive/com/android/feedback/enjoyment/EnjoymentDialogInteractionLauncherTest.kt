package apptentive.com.android.feedback.enjoyment

import apptentive.com.android.TestCase
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.feedback.EngagementResult
import apptentive.com.android.feedback.engagement.EngageArgs
import apptentive.com.android.feedback.engagement.EngagementContextFactory
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.MockEngagementContext
import apptentive.com.android.feedback.engagement.MockEngagementContextFactory
import apptentive.com.android.feedback.interactions.enjoyment.EnjoymentDialogInteraction
import apptentive.com.android.feedback.interactions.enjoyment.EnjoymentDialogInteractionLauncher
import org.junit.Test

class EnjoymentDialogInteractionLauncherTest : TestCase() {
    private val launcher = EnjoymentDialogInteractionLauncher()

    @Test
    fun `launchInteraction always passes whereEvent as-is`() {
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
            EnjoymentDialogInteraction("LD", "Title", "Yes", "No", "Dismiss"), "LD Event"
        )

        assertResults(
            EngageArgs(
                Event("com.apptentive", "EnjoymentDialog", "launch"),
                "LD",
                whereEvent = "LD Event"
            )
        )
    }
}
