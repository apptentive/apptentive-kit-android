package apptentive.com.android.ratings.ratingdialog

import apptentive.com.android.TestCase
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.core.Provider
import apptentive.com.android.feedback.EngagementResult
import apptentive.com.android.feedback.engagement.EngageArgs
import apptentive.com.android.feedback.engagement.EngagementCallback
import apptentive.com.android.feedback.engagement.EngagementContext
import apptentive.com.android.feedback.engagement.AndroidEngagementContextFactory
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.InvocationCallback
import apptentive.com.android.feedback.engagement.MockEngagementContext
import apptentive.com.android.feedback.ratingdialog.RatingDialogInteraction
import apptentive.com.android.feedback.ratingdialog.RatingDialogInteractionProvider
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

        DependencyProvider.register(object : Provider<AndroidEngagementContextFactory> {
            override fun get(): AndroidEngagementContextFactory {
                return object : AndroidEngagementContextFactory {
                    override fun engagementContext(): EngagementContext {
                        return createEngagementContext(
                            null,
                            { EngagementResult.InteractionShown(targetInteractionId) }
                        )
                    }
                }
            }
        })
        DependencyProvider.register(RatingDialogInteractionProvider(interaction))
        val viewModel = createViewModel()

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

    private fun createViewModel(): RatingDialogViewModel {
        return RatingDialogViewModel()
    }

    private fun createEngagementContext(
        onEngage: EngagementCallback? = null,
        onInvoke: InvocationCallback? = null
    ) = MockEngagementContext(
        // record engagement args for every engage call
        onEngage = { args ->
            addResult(args)
            onEngage?.invoke(args)
                ?: EngagementResult.InteractionNotShown("No runnable interactions")
        },
        // record invocations for every engage call
        onInvoke = { invocations ->
            addResult(invocations)
            onInvoke?.invoke(invocations)
                ?: EngagementResult.InteractionNotShown("No runnable interactions")
        },
        // we don't expect payloads here
        onSendPayload = { payload ->
            throw AssertionError("We didn't expect any payloads here but this one slipped though: $payload")
        }
    )

    //endregion
}