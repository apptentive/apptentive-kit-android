package apptentive.com.android.feedback.initiator

import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.core.DependencyProvider.register
import apptentive.com.android.core.Provider
import apptentive.com.android.feedback.EngagementResult
import apptentive.com.android.feedback.engagement.EngageArgs
import apptentive.com.android.feedback.engagement.EngagementContext
import apptentive.com.android.feedback.engagement.EngagementContextFactory
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.MockEngagementContext

class InitiatorInteractionLauncherTest : apptentive.com.android.TestCase() {
    private val launcher = InitiatorInteractionLauncher()

    @org.junit.Test
    fun `launchInteraction always passes null whereEvent regardless of input`() {
        register(
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

internal class MockEngagementContextFactory(val getEngagementContext: () -> EngagementContext) :
    Provider<EngagementContextFactory> {
    override fun get(): EngagementContextFactory {
        return object : EngagementContextFactory {
            override fun engagementContext(): EngagementContext {
                return getEngagementContext()
            }
        }
    }
}
