package apptentive.com.android.feedback.ui

import apptentive.com.android.concurrent.Executors
import apptentive.com.android.concurrent.ImmediateExecutor
import apptentive.com.android.feedback.EngagementResult
import apptentive.com.android.feedback.engagement.Engagement
import apptentive.com.android.feedback.engagement.EngagementContext
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.model.payloads.ExtendedData
import apptentive.com.android.feedback.ui.EnjoymentDialogViewModel.Companion.CODE_POINT_CANCEL
import apptentive.com.android.feedback.ui.EnjoymentDialogViewModel.Companion.CODE_POINT_DISMISS
import apptentive.com.android.feedback.ui.EnjoymentDialogViewModel.Companion.CODE_POINT_NO
import apptentive.com.android.feedback.ui.EnjoymentDialogViewModel.Companion.CODE_POINT_YES
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class EnjoymentDialogViewModelTest {
    @Test
    fun testEvents() {
        val engagementCalls = mutableListOf<EngagementCall>()

        val engagement = object : Engagement {
            override fun engage(
                context: EngagementContext,
                event: Event,
                interactionId: String?,
                data: Map<String, Any>?,
                customData: Map<String, Any>?,
                extendedData: List<ExtendedData>?
            ): EngagementResult {
                engagementCalls.add(
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
                executors = Executors(ImmediateExecutor, ImmediateExecutor)
            ),
            interaction = interaction
        )
        viewModel.onYesButton()
        viewModel.onNoButton()
        viewModel.onDismissButton()
        viewModel.onCancel()

        assertThat(engagementCalls).isEqualTo(
            listOf(
                createCall(CODE_POINT_YES, interactionId = interactionId),
                createCall(CODE_POINT_NO, interactionId = interactionId),
                createCall(CODE_POINT_DISMISS, interactionId = interactionId),
                createCall(CODE_POINT_CANCEL, interactionId = interactionId)
            )
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

