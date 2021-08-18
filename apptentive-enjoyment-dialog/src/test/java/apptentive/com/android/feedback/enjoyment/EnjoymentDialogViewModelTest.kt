package apptentive.com.android.feedback.enjoyment

import apptentive.com.android.TestCase
import apptentive.com.android.feedback.EngagementResult
import apptentive.com.android.feedback.engagement.EngageArgs
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.MockEngagementContext
import apptentive.com.android.feedback.enjoyment.EnjoymentDialogViewModel.Companion.CODE_POINT_CANCEL
import apptentive.com.android.feedback.enjoyment.EnjoymentDialogViewModel.Companion.CODE_POINT_DISMISS
import apptentive.com.android.feedback.enjoyment.EnjoymentDialogViewModel.Companion.CODE_POINT_NO
import apptentive.com.android.feedback.enjoyment.EnjoymentDialogViewModel.Companion.CODE_POINT_YES
import org.junit.Test

class EnjoymentDialogViewModelTest : TestCase() {
    @Test
    fun testEvents() {
        val context = MockEngagementContext(
            onEngage = { args ->
                addResult(args)
                EngagementResult.Failure("No runnable interactions")
            },
            onSendPayload = { payload ->
                throw AssertionError("We didn't expect any payloads here but this one slipped though: $payload")
            }
        )
        val interactionId = "123456789"
        val interaction = EnjoymentDialogInteraction(
            id = interactionId,
            title = "Title",
            yesText = "Yes",
            noText = "No",
            dismissText = "Dismiss"
        )
        val viewModel = EnjoymentDialogViewModel(
            context = context,
            interaction = interaction
        )

        viewModel.onYesButton()
        assertResults(
            createCall(CODE_POINT_YES, interactionId = interactionId)
        )

        viewModel.onNoButton()
        assertResults(
            createCall(CODE_POINT_NO, interactionId = interactionId)
        )

        viewModel.onDismiss()
        assertResults(
            createCall(CODE_POINT_DISMISS, interactionId = interactionId)
        )

        viewModel.onCancel()
        assertResults(
            createCall(CODE_POINT_CANCEL, interactionId = interactionId)
        )
    }

    private fun createCall(codePoint: String, interactionId: String) =
        EngageArgs(
            event = Event.internal(codePoint, interaction = "EnjoymentDialog"),
            interactionId = interactionId
        )
}
