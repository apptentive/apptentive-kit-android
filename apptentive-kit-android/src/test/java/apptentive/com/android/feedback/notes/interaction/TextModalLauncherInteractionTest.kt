package apptentive.com.android.feedback.notes.interaction

import apptentive.com.android.TestCase
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.feedback.EngagementResult
import apptentive.com.android.feedback.engagement.EngageArgs
import apptentive.com.android.feedback.engagement.EngagementContextFactory
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.MockEngagementContext
import apptentive.com.android.feedback.engagement.MockEngagementContextFactory
import apptentive.com.android.feedback.textmodal.TextModalInteraction
import apptentive.com.android.feedback.textmodal.TextModalInteractionLauncher
import apptentive.com.android.ui.DialogPosition
import org.junit.Test

class TextModalLauncherInteractionTest : TestCase() {
    private val launcher = TextModalInteractionLauncher()

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
        try {
            launcher.launchInteraction(
                DependencyProvider.of<EngagementContextFactory>().engagementContext(),
                TextModalInteraction(
                    "TM",
                    "Title",
                    "Body",
                    100,
                    null,
                    listOf(),
                    DialogPosition.CENTER,
                    null
                ),
                "TM_Event"
            )
        } catch (e: Exception) {
        }

        assertResults(
            EngageArgs(
                Event("com.apptentive", "TextModal", "launch"),
                "TM",
                whereEvent = "TM_Event"
            )
        )
    }
}
