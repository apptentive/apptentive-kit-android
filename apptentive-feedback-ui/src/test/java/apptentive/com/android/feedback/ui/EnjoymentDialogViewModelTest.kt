package apptentive.com.android.feedback.ui

import apptentive.com.android.feedback.EngagementResult
import apptentive.com.android.feedback.engagement.Engagement
import apptentive.com.android.feedback.engagement.EngagementContext
import apptentive.com.android.feedback.engagement.Event
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
            override fun engage(context: EngagementContext, event: Event): EngagementResult {
                events.add(event)
                return EngagementResult.Success
            }
        }
        val viewModel = EnjoymentDialogViewModel(EngagementContext(engagement))
        viewModel.onYesButton()
        viewModel.onNoButton()
        viewModel.onDismissButton()
        viewModel.onCancel()

        assertThat(events).isEqualTo(
            listOf(
                Event.internal(CODE_POINT_YES),
                Event.internal(CODE_POINT_NO),
                Event.internal(CODE_POINT_DISMISS),
                Event.internal(CODE_POINT_CANCEL)
            )
        )
    }
}

