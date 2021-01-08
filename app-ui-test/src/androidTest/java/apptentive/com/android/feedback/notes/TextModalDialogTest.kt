package apptentive.com.android.feedback.notes

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import apptentive.com.android.concurrent.Executor
import apptentive.com.android.concurrent.Executors
import apptentive.com.android.feedback.EngagementResult
import apptentive.com.android.feedback.engagement.Engagement
import apptentive.com.android.feedback.engagement.EngagementContext
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.criteria.Invocation
import apptentive.com.android.feedback.model.payloads.ExtendedData
import apptentive.com.android.feedback.model.payloads.Payload
import apptentive.com.android.feedback.notes.interaction.TextModalInteraction
import apptentive.com.android.feedback.notes.view.TextModalDialog
import apptentive.com.android.feedback.notes.viewmodel.TextModalViewModel
import apptentive.com.android.feedback.payload.PayloadSender
import apptentive.com.app.test.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TextModalDialogTest {
    @get:Rule
    var activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testLauncher() {
        activityRule.scenario.onActivity { activity ->
            val context = createEngagementContext()
            val interaction = TextModalInteraction(
                id = "id",
                title = "Title",
                body = "Body",
                actions = listOf(
                    TextModalInteraction.Action.Dismiss(
                        id = "action_id",
                        label = "Dismiss"
                    )
                )
            )

            val viewModel = TextModalViewModel(context, interaction)


            val dialog = TextModalDialog(activity, viewModel)
            dialog.show()

            onView(withId(R.id.apptentive_note_title))
                .check(matches(withText("Title")))
        }
    }

    private fun createEngagementContext(): EngagementContext {
        return EngagementContext(
            engagement = object : Engagement {
                override fun engage(
                    context: EngagementContext,
                    event: Event,
                    interactionId: String?,
                    data: Map<String, Any?>?,
                    customData: Map<String, Any?>?,
                    extendedData: List<ExtendedData>?
                ): EngagementResult {
                    return EngagementResult.Failure("No runnable interactions")
                }

                override fun engage(
                    context: EngagementContext,
                    invocations: List<Invocation>
                ): EngagementResult {
                    return EngagementResult.Failure("No runnable interactions")
                }
            },
            payloadSender = object : PayloadSender {
                override fun sendPayload(payload: Payload) {
                }
            },
            executors = Executors(ImmediateExecutor, ImmediateExecutor)
        )
    }

    private object ImmediateExecutor : Executor {
        override fun execute(task: () -> Unit) {
            task()
        }
    }
}