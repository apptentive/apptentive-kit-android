package apptentive.com.android.feedback.notes.viewmodel

import apptentive.com.android.TestCase
import apptentive.com.android.feedback.EngagementResult
import apptentive.com.android.feedback.engagement.EngageArgs
import apptentive.com.android.feedback.engagement.EngagementCallback
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.InvocationCallback
import apptentive.com.android.feedback.engagement.MockEngagementContext
import apptentive.com.android.feedback.engagement.criteria.InvocationConverter
import apptentive.com.android.feedback.model.InvocationData
import apptentive.com.android.feedback.notes.interaction.TextModalInteraction
import org.junit.Test

class TextModalViewModelTest : TestCase() {
    private val interactionId = "123456789"

    private val invocations = listOf<InvocationData>()

    private val interaction = TextModalInteraction(
        id = interactionId,
        title = "Title",
        body = "Body",
        actions = listOf(
            TextModalInteraction.Action.Invoke(
                id = "action_invoke",
                label = "Invoke Action",
                invocations = invocations
            ),
            TextModalInteraction.Action.Event(
                id = "action_event",
                label = "Event Action",
                event = Event.internal("my_event", interaction = "TextModal")
            ),
            TextModalInteraction.Action.Dismiss(
                id = "action_dismiss",
                label = "Dismiss Action"
            )
        )
    )

    @Test
    fun testEvents() {
        val context = MockEngagementContext(
            onEngage = { args ->
                addResult(args)
                EngagementResult.Failure("No runnable interactions")
            },
            onInvoke = { invocations ->
                addResult(invocations)
                EngagementResult.Failure("No runnable interactions")
            },
            onSendPayload = { payload ->
                throw AssertionError("We didn't expect any payloads here but this one slipped though: $payload")
            }
        )
        val interactionId = "123456789"
        val invocations = listOf<InvocationData>()
        val interaction = TextModalInteraction(
            id = interactionId,
            title = "Title",
            body = "Body",
            actions = listOf(
                TextModalInteraction.Action.Invoke(
                    id = "action_1",
                    label = "Action 1",
                    invocations = invocations
                ),
                TextModalInteraction.Action.Event(
                    id = "action_2",
                    label = "Action 2",
                    event = Event.internal("my_event", interaction = "TextModal")
                ),
                TextModalInteraction.Action.Dismiss(
                    id = "action_3",
                    label = "Action 3"
                )
            )
        )
        val viewModel = TextModalViewModel(
            context = context,
            interaction = interaction
        )
        viewModel.onDismiss = { addResult("onDismiss") }

        viewModel.invokeAction(id = "action_1")
        assertResults(
            invocations.map(InvocationConverter::convert),
            "onDismiss"
        )

        viewModel.invokeAction(id = "action_2")
        assertResults(
            EngageArgs(
                event = Event.internal("my_event", interaction = "TextModal"),
                interactionId = interactionId
            ),
            "onDismiss"
        )

        viewModel.invokeAction(id = "action_3")
        assertResults(
            "onDismiss"
        )

        viewModel.onCancel()

        assertResults()
    }

    private fun createViewModel(
        onEngage: EngagementCallback? = null,
        onInvoke: InvocationCallback? = null
    ): TextModalViewModel {
        val context = createEngagementContext(onEngage, onInvoke)
        val viewModel = TextModalViewModel(
            context = context,
            interaction = interaction
        )
        viewModel.onDismiss = { addResult("onDismiss") }
        return viewModel
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
}