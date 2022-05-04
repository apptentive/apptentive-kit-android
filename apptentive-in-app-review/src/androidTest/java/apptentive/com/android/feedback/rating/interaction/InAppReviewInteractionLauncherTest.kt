package apptentive.com.android.feedback.rating.interaction

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import apptentive.com.android.TestCase
import apptentive.com.android.concurrent.Executors
import apptentive.com.android.concurrent.ImmediateExecutor
import apptentive.com.android.feedback.EngagementResult
import apptentive.com.android.feedback.engagement.EngagementCallback
import apptentive.com.android.feedback.engagement.EngagementContext
import apptentive.com.android.feedback.engagement.MockEngagementContext
import apptentive.com.android.feedback.engagement.PayloadSenderCallback
import apptentive.com.android.feedback.engagement.interactions.InteractionId
import apptentive.com.android.feedback.rating.reviewmanager.InAppReviewCallback
import apptentive.com.android.feedback.rating.reviewmanager.InAppReviewManager
import apptentive.com.android.feedback.rating.reviewmanager.InAppReviewManagerFactory
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.AssertionError

@RunWith(AndroidJUnit4::class)
class InAppReviewInteractionLauncherTest : TestCase() {

    // Context of the app under test.
    private var appContext = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun testInAppReviewNotSupported() {
        val launcher: InAppReviewInteractionLauncher =
            InAppReviewInteractionLauncher(MockInAppReviewManagerFactory(MockInAppReviewManager.unSupported()))
        launcher.launchInteraction(
            createEngagementContext(onEngage = {
                addResult("engage ${it.event}")
                EngagementResult.InteractionShown(it.interactionId as InteractionId)
            }),
            InAppReviewInteraction("58eebbf2127096704e0000d0")
        )
        assertResults(
            "engage com.apptentive#InAppRatingDialog#launch",
            "engage com.apptentive#InAppRatingDialog#request",
            "engage com.apptentive#InAppRatingDialog#not_supported"
        )
    }

    @Test
    fun testInAppReviewFailed() {
        val launcher: InAppReviewInteractionLauncher =
            InAppReviewInteractionLauncher(MockInAppReviewManagerFactory(MockInAppReviewManager.failed("Something went wrong")))
        launcher.launchInteraction(
            createEngagementContext(onEngage = {
                addResult("engage ${it.event}")
                EngagementResult.InteractionNotShown("Something went wrong")
            }),
            InAppReviewInteraction("58eebbf2127096704e0000d0")
        )
        assertResults(
            "engage com.apptentive#InAppRatingDialog#launch",
            "engage com.apptentive#InAppRatingDialog#request",
            "engage com.apptentive#InAppRatingDialog#not_shown"
        )
    }

    @Test
    fun testInAppReviewSupportedLaunchSucceed() {
        val launcher: InAppReviewInteractionLauncher =
            InAppReviewInteractionLauncher(MockInAppReviewManagerFactory(MockInAppReviewManager.successful()))
        launcher.launchInteraction(
            createEngagementContext(context = appContext, onEngage = {
                addResult("engage ${it.event}")
                EngagementResult.InteractionShown(it.interactionId as InteractionId)
            }),
            InAppReviewInteraction("58eebbf2127096704e0000d0")
        )
        assertResults(
            "engage com.apptentive#InAppRatingDialog#launch",
            "engage com.apptentive#InAppRatingDialog#request",
            "engage com.apptentive#InAppRatingDialog#shown"
        )
    }

    private fun createEngagementContext(context: Context = appContext, onEngage: EngagementCallback?): EngagementContext {
        val mockEngagementContext = createMockEngagementContext(
            onEngage = onEngage
        )
        return EngagementContext(
            engagement = mockEngagementContext.getEngagement(),
            payloadSender = mockEngagementContext.getPayloadSender(),
            executors = Executors(ImmediateExecutor, ImmediateExecutor)
        )
    }

    private fun createMockEngagementContext(
        onEngage: EngagementCallback? = null,
        onSendPayload: PayloadSenderCallback? = null
    ) = MockEngagementContext(
        onEngage = onEngage ?: { args ->
            addResult(args)
            EngagementResult.InteractionShown(args.interactionId as InteractionId)
        },
        onSendPayload = onSendPayload ?: { payload ->
            addResult(payload.toJson())
        }
    )
}

class MockInAppReviewManager(val supported: Boolean, val errorMessage: String?) :
    InAppReviewManager {
    override fun startReviewFlow(callback: InAppReviewCallback) {
        if (supported) {
            if (errorMessage != null) {
                callback.onReviewFlowFailed(errorMessage)
            } else {
                callback.onReviewComplete()
            }
        } else {
            throw AssertionError("Should not get there")
        }
    }

    override fun isInAppReviewSupported(): Boolean = supported

    companion object {
        fun successful(): InAppReviewManager = MockInAppReviewManager(true, null)

        fun failed(errorMessage: String): InAppReviewManager = MockInAppReviewManager(true, errorMessage)

        fun unSupported(): InAppReviewManager = MockInAppReviewManager(false, null)
    }
}

class MockInAppReviewManagerFactory(private val manager: InAppReviewManager) :
    InAppReviewManagerFactory {
    override fun createReviewManager(context: Context): InAppReviewManager = manager
}
