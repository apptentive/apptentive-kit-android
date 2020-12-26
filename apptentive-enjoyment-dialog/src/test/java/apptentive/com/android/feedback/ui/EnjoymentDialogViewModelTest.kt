package apptentive.com.android.feedback.ui

import apptentive.com.android.TestCase
import apptentive.com.android.concurrent.Executors
import apptentive.com.android.concurrent.ImmediateExecutor
import apptentive.com.android.feedback.EngagementResult
import apptentive.com.android.feedback.engagement.Engagement
import apptentive.com.android.feedback.engagement.EngagementContext
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.criteria.Invocation
import apptentive.com.android.feedback.model.payloads.ExtendedData
import apptentive.com.android.feedback.model.payloads.Payload
import apptentive.com.android.feedback.payload.PayloadSender
import apptentive.com.android.feedback.ui.EnjoymentDialogViewModel.Companion.CODE_POINT_CANCEL
import apptentive.com.android.feedback.ui.EnjoymentDialogViewModel.Companion.CODE_POINT_DISMISS
import apptentive.com.android.feedback.ui.EnjoymentDialogViewModel.Companion.CODE_POINT_NO
import apptentive.com.android.feedback.ui.EnjoymentDialogViewModel.Companion.CODE_POINT_YES
import org.junit.Test

class EnjoymentDialogViewModelTest : TestCase() {
    @Test
    fun testEvents() {
        val engagement = object : Engagement {
            override fun engage(
                context: EngagementContext,
                event: Event,
                interactionId: String?,
                data: Map<String, Any>?,
                customData: Map<String, Any>?,
                extendedData: List<ExtendedData>?
            ): EngagementResult {
                addResult(
                    EngagementCall(
                        event = event,
                        interactionId = interactionId,
                        data = data,
                        customData = customData,
                        extendedData = extendedData
                    )
                )
                return EngagementResult.Success
            }

            override fun engage(
                context: EngagementContext,
                invocations: List<Invocation>
            ): EngagementResult {
                TODO("Not yet implemented")
            }
        }
        val interactionId = "123456789"
        val interaction = EnjoymentDialogInteraction(
            id = interactionId,
            title = "Title",
            yesText = "Yes",
            noText = "No",
            dismissText = "Dismiss"
        )
        val viewModel = EnjoymentDialogViewModel(
            context = EngagementContext(
                engagement = engagement,
                payloadSender = object : PayloadSender {
                    override fun sendPayload(payload: Payload) {
                        throw AssertionError("We didn't expect any payloads here but this one slipped though: $payload")
                    }
                },
                executors = Executors(ImmediateExecutor, ImmediateExecutor)
            ),
            interaction = interaction
        )
        viewModel.onDismiss = { addResult("onDismiss") }

        viewModel.onYesButton()
        assertResults(
            createCall(CODE_POINT_YES, interactionId = interactionId),
            "onDismiss"
        )

        viewModel.onNoButton()
        assertResults(
            createCall(CODE_POINT_NO, interactionId = interactionId),
            "onDismiss"
        )

        viewModel.onDismissButton()
        assertResults(
            createCall(CODE_POINT_DISMISS, interactionId = interactionId),
            "onDismiss"
        )

        viewModel.onCancel()
        assertResults(
            createCall(CODE_POINT_CANCEL, interactionId = interactionId)
        )
    }

    private fun createCall(codePoint: String, interactionId: String) =
        EngagementCall(
            event = Event.internal(codePoint, interaction = "EnjoymentDialog"),
            interactionId = interactionId
        )

    private data class EngagementCall(
        val event: Event,
        val interactionId: String? = null,
        val data: Map<String, Any>? = null,
        val customData: Map<String, Any>? = null,
        val extendedData: List<ExtendedData>? = null
    )
}

