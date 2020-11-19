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
        val events = mutableListOf<Event>()

        val engagement = object : Engagement {
            override fun engage(
                context: EngagementContext,
                event: Event,
                interactionId: String?,
                data: Map<String, Any>?,
                customData: Map<String, Any>?,
                extendedData: List<ExtendedData>?
            ): EngagementResult {
                events.add(event)
                return EngagementResult.Success
            }
        }
        val interaction = EnjoymentDialogInteraction(
            id = "id",
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

        assertThat(events).isEqualTo(
            listOf(
                Event.internal(CODE_POINT_YES, interaction = "EnjoymentDialog"),
                Event.internal(CODE_POINT_NO, interaction = "EnjoymentDialog"),
                Event.internal(CODE_POINT_DISMISS, interaction = "EnjoymentDialog"),
                Event.internal(CODE_POINT_CANCEL, interaction = "EnjoymentDialog")
            )
        )
    }
}

