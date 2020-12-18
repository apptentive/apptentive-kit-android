package apptentive.com.android.feedback.survey.interaction

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import apptentive.com.android.TestCase
import apptentive.com.android.feedback.EngagementResult
import apptentive.com.android.feedback.GenerateUUIDRule
import apptentive.com.android.feedback.MockTimeRule
import apptentive.com.android.feedback.engagement.EngageArgs
import apptentive.com.android.feedback.engagement.EngagementCallback
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.MockEngagementContext
import apptentive.com.android.feedback.engagement.PayloadSenderCallback
import apptentive.com.android.feedback.survey.model.MultiChoiceQuestion
import apptentive.com.android.feedback.survey.model.SurveyModel
import apptentive.com.android.feedback.survey.model.SurveyQuestion
import apptentive.com.android.feedback.survey.model.createMultiChoiceQuestion
import apptentive.com.android.feedback.survey.model.createRangeQuestion
import apptentive.com.android.feedback.survey.model.createSingleLineQuestion
import apptentive.com.android.toProperJson
import org.junit.Rule
import org.junit.Test

class SurveyInteractionLauncherTest : TestCase() {
    @get:Rule
    val uuidRule = GenerateUUIDRule()

    @get:Rule
    val timeRule = MockTimeRule(currentTime = 1000.0, utcOffset = -18000)

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Test
    fun testViewModel() {
        val context = createEngagementContext()
        val model = createSurveyModel(
            listOf(
                createSingleLineQuestion(id = "id_1", answer = "text"),
                createRangeQuestion(id = "id_2", selectedIndex = 5),
                createMultiChoiceQuestion(
                    id = "id_3",
                    answerChoiceConfigs = listOf(
                        MultiChoiceQuestion.AnswerChoiceConfiguration(
                            id = "choice_1",
                            type = MultiChoiceQuestion.ChoiceType.select_option,
                            title = "Title 1"
                        ),
                        MultiChoiceQuestion.AnswerChoiceConfiguration(
                            id = "choice_2",
                            type = MultiChoiceQuestion.ChoiceType.select_other,
                            title = "Title 2"
                        )
                    ),
                    answer = listOf(
                        MultiChoiceQuestion.Answer.Choice(id = "choice_1", checked = true),
                        MultiChoiceQuestion.Answer.Choice(
                            id = "choice_2",
                            checked = true,
                            value = "Other"
                        )
                    ),
                    minSelections = 1,
                    maxSelections = 2
                )
            )
        )
        val viewModel = SurveyInteractionLauncher().createSurveyViewModel(
            context = context,
            model = model,
            interactionId = "interaction_id"
        )

        viewModel.submit()

        assertResults(
            // survey response payload
            toProperJson(
                "{'response':{'id':'interaction_id','answers':{'id_1':[{'value':'text'}],'id_2':[{'value':5}],'id_3':[{'id':'choice_1'},{'id':'choice_2','value':'Other'}]},'client_created_at':1000.0,'client_created_at_utc_offset':-18000,'nonce':'xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx'}}"
            ),

            // "submit" event
            EngageArgs(
                event = Event.internal(name = "submit", interaction = "Survey"),
                interactionId = "interaction_id"
            )
        )
    }

    private fun createEngagementContext(
        onEngage: EngagementCallback? = null,
        onSendPayload: PayloadSenderCallback? = null
    ) = MockEngagementContext(
        onEngage = onEngage ?: { args ->
            addResult(args)
            EngagementResult.Success
        },
        onSendPayload = onSendPayload ?: { payload ->
            addResult(payload.toJson())
        }
    )

    private fun createSurveyModel(questions: List<SurveyQuestion<*>>? = null): SurveyModel {
        return SurveyModel(questions = questions ?: emptyList(), validationError = null)
    }
}