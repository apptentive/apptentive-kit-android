package apptentive.com.android.ratings.ratingdialog

import apptentive.com.android.TestCase
import apptentive.com.android.feedback.EngagementResult
import apptentive.com.android.feedback.engagement.EngageArgs
import apptentive.com.android.feedback.engagement.EngagementCallback
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.InvocationCallback
import apptentive.com.android.feedback.engagement.MockEngagementContext
import apptentive.com.android.feedback.ratingdialog.RatingDialogInteraction
import apptentive.com.android.feedback.ratingdialog.RatingDialogViewModel
import org.junit.Test

class RatingDialogViewModelTest : TestCase() {
    private val interactionId = "123456789"

    private val interaction = RatingDialogInteraction(
        id = interactionId,
        title = "Title",
        body = "Body",
        rateText = "Rate",
        remindText = "Remind",
        declineText = "Decline"
    )

    //region Interactions

    @Test
    fun testInvokeInteractions() {
        val targetInteractionId = "target_id"
        val viewModel = createViewModel(
            onInvoke = {
                // trick it to think an interaction has been invoked
                EngagementResult.Success(targetInteractionId)
            }
        )

        viewModel.onRateButton()
        viewModel.onRemindButton()
        viewModel.onDeclineButton()
        viewModel.onCancel()

        val engageResults = listOf(
             EngageArgs(
                event = Event.internal("rate", "RatingDialog"),
                interactionId = interactionId,
            ),
            EngageArgs(
                event = Event.internal("remind", "RatingDialog"),
                interactionId = interactionId,
            ),
            EngageArgs(
                event = Event.internal("decline", "RatingDialog"),
                interactionId = interactionId,
            ),
            EngageArgs(
                event = Event.internal("cancel", "RatingDialog"),
                interactionId = interactionId,
            ),
        )

        assertResults(
            // engage "interaction" event
            *engageResults.toTypedArray()
        )
    }

    //endregion

    //region Helpers

    private fun createViewModel(
        onEngage: EngagementCallback? = null,
        onInvoke: InvocationCallback? = null
    ): RatingDialogViewModel {
        val context = createEngagementContext(onEngage, onInvoke)
        return RatingDialogViewModel(
            context = context,
            interaction = interaction
        )
    }

    private fun createEngagementContext(
        onEngage: EngagementCallback? = null,
        onInvoke: InvocationCallback? = null
    ) = MockEngagementContext(
        // record engagement args for every engage call
        onEngage = { args ->
            addResult(args)
            onEngage?.invoke(args)
                ?: EngagementResult.Failure("No runnable interactions")
        },
        // record invocations for every engage call
        onInvoke = { invocations ->
            addResult(invocations)
            onInvoke?.invoke(invocations)
                ?: EngagementResult.Failure("No runnable interactions")
        },
        // we don't expect payloads here
        onSendPayload = { payload ->
            throw AssertionError("We didn't expect any payloads here but this one slipped though: $payload")
        }
    )

    //endregion
}