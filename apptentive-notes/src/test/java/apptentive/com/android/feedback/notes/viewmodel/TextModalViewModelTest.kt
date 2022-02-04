package apptentive.com.android.feedback.notes.viewmodel

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
import apptentive.com.android.feedback.engagement.criteria.InvocationConverter
import apptentive.com.android.feedback.model.InvocationData
import apptentive.com.android.feedback.textmodal.TextModalInteraction
import apptentive.com.android.feedback.textmodal.TextModalInteractionProvider
import apptentive.com.android.feedback.textmodal.TextModalViewModel
import com.google.common.truth.Truth.assertThat
import org.junit.Before
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
                id = ACTION_ID_INTERACTION,
                label = ACTION_LABEL_INTERACTION,
                invocations = invocations
            ),
            TextModalInteraction.Action.Event(
                id = ACTION_ID_EVENT,
                label = ACTION_LABEL_EVENT,
                event = Event.internal(TARGET_EVENT, interaction = "TextModal")
            ),
            TextModalInteraction.Action.Dismiss(
                id = ACTION_ID_DISMISS,
                label = ACTION_LABEL_DISMISS
            )
        )
    )

    @Before
    fun start() {
        DependencyProvider.register(TextModalInteractionProvider(interaction))
    }

    //region Interaction

    @Test
    fun testInvokeInteraction() {
        val targetInteractionId = "target_id"
        DependencyProvider.register(MockEngagementContextFactory {
            createEngagementContext(
                null,
                { EngagementResult.InteractionShown(targetInteractionId) }
            )
        })

        val viewModel = createViewModel()

        // check action title
        val action = viewModel.actions[0]
        assertThat(action.title).isEqualTo(ACTION_LABEL_INTERACTION)

        // invoke action
        action.invoke()

        assertResults(
            // attempt to invoke an interaction
            invocations.map(InvocationConverter::convert),
            // engage "interaction" event
            EngageArgs(
                event = Event.internal("interaction", "TextModal"),
                interactionId = interactionId,
                data = mapOf(
                    "action_id" to ACTION_ID_INTERACTION,
                    "label" to ACTION_LABEL_INTERACTION,
                    "position" to 0,
                    "invoked_interaction_id" to targetInteractionId
                )
            ),
            // dismiss UI
            RESULT_DISMISS_UI
        )
    }

    @Test
    fun testInvokeMissingInteraction() {
        DependencyProvider.register(MockEngagementContextFactory {
            createEngagementContext(
                null,
                {
                    EngagementResult.InteractionNotShown("No runnable interactions")
                }
            )
        })
        val viewModel = createViewModel()

        // check action title
        val action = viewModel.actions[0]
        assertThat(action.title).isEqualTo(ACTION_LABEL_INTERACTION)

        // invoke action
        action.invoke()

        // check results
        assertResults(
            // attempt to invoke an interaction
            invocations.map(InvocationConverter::convert),
            // engage "interaction" event
            EngageArgs(
                event = Event.internal("interaction", "TextModal"),
                interactionId = interactionId,
                data = mapOf(
                    "action_id" to ACTION_ID_INTERACTION,
                    "label" to ACTION_LABEL_INTERACTION,
                    "position" to 0,
                    "invoked_interaction_id" to null
                )
            ),
            // dismiss UI
            RESULT_DISMISS_UI
        )
    }

    //endregion

    //region Event

    @Test
    fun testEventAction() {
        // NOTE: this is not supported on the backend yet!!!
        val targetInteractionId = "target_id"

        DependencyProvider.register(MockEngagementContextFactory {
            createEngagementContext(
                {
                    // trick it to think an interaction has been invoked
                    if (it.event.name == TARGET_EVENT) EngagementResult.InteractionShown(targetInteractionId)
                    else EngagementResult.InteractionNotShown("No runnable interactions")
                },
                null
            )
        })

        val viewModel = createViewModel()

        // check action title
        val action = viewModel.actions[1]
        assertThat(action.title).isEqualTo(ACTION_LABEL_EVENT)

        // invoke action
        action.invoke()

        // check results
        assertResults(
            // engage event
            EngageArgs(
                event = Event.internal(TARGET_EVENT, interaction = "TextModal"),
                interactionId = interactionId
            ),
            // engage "interaction" event
            EngageArgs(
                event = Event.internal("event", "TextModal"),
                interactionId = interactionId,
                data = mapOf(
                    "action_id" to ACTION_ID_EVENT,
                    "label" to ACTION_LABEL_EVENT,
                    "position" to 1,
                    "invoked_interaction_id" to targetInteractionId
                )
            ),
            // dismiss UI
            RESULT_DISMISS_UI
        )
    }

    @Test
    fun testMissingEventAction() {
        // NOTE: this is not supported on the backend yet!!!

        DependencyProvider.register(MockEngagementContextFactory {
            createEngagementContext(
                {
                    // no interactions to invoke
                    EngagementResult.InteractionNotShown("No runnable interactions")
                },
                null
            )
        })

        val viewModel = createViewModel()

        // check action title
        val action = viewModel.actions[1]
        assertThat(action.title).isEqualTo(ACTION_LABEL_EVENT)

        // invoke action
        action.invoke()

        // check results
        assertResults(
            // engage event
            EngageArgs(
                event = Event.internal(TARGET_EVENT, interaction = "TextModal"),
                interactionId = interactionId
            ),
            // engage "interaction" event
            EngageArgs(
                event = Event.internal("event", "TextModal"),
                interactionId = interactionId,
                data = mapOf(
                    "action_id" to ACTION_ID_EVENT,
                    "label" to ACTION_LABEL_EVENT,
                    "position" to 1,
                    "invoked_interaction_id" to null
                )
            ),
            // dismiss UI
            RESULT_DISMISS_UI
        )
    }

    //endregion

    //region Dismiss

    @Test
    fun testDismissAction() {
        DependencyProvider.register(MockEngagementContextFactory {
            createEngagementContext(null, null)
        })
        val viewModel = createViewModel()

        // check action title
        val action = viewModel.actions[2]
        assertThat(action.title).isEqualTo(ACTION_LABEL_DISMISS)

        // invoke action
        action.invoke()

        // check results
        assertResults(
            // engage "dismiss" event
            EngageArgs(
                event = Event.internal("dismiss", "TextModal"),
                interactionId = interactionId,
                data = mapOf(
                    "action_id" to ACTION_ID_DISMISS,
                    "label" to ACTION_LABEL_DISMISS,
                    "position" to 2
                )
            ),
            // dismiss UI
            RESULT_DISMISS_UI
        )
    }

    //endregion

    //region Cancel

    @Test
    fun testCancel() {
        DependencyProvider.register(MockEngagementContextFactory {
            createEngagementContext(null, null)
        })
        val viewModel = createViewModel()

        // invoke action
        viewModel.onCancel()

        // check results
        assertResults(
            // engage "dismiss" event
            EngageArgs(
                event = Event.internal("cancel", "TextModal"),
                interactionId = interactionId
            )
        )
    }

    //endregion

    //region Helpers

    private fun createViewModel(): TextModalViewModel {
        val viewModel = TextModalViewModel()
        viewModel.onDismiss = { addResult(RESULT_DISMISS_UI) }
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

    //region Companion

    companion object {
        private const val ACTION_ID_INTERACTION = "action_invoke"
        private const val ACTION_LABEL_INTERACTION = "Invoke Action"

        private const val ACTION_ID_EVENT = "action_event"
        private const val ACTION_LABEL_EVENT = "Event Action"

        private const val ACTION_ID_DISMISS = "action_dismiss"
        private const val ACTION_LABEL_DISMISS = "Dismiss Action"

        private const val TARGET_EVENT = "my_event"

        private const val RESULT_DISMISS_UI = "Dismiss UI"
    }

    //endregion
}

class MockEngagementContextFactory(val getEngagementContext: () -> EngagementContext) : Provider<AndroidEngagementContextFactory> {
    override fun get(): AndroidEngagementContextFactory {
        return object : AndroidEngagementContextFactory {
            override fun engagementContext(): EngagementContext {
                return getEngagementContext()
            }
        }
    }
}