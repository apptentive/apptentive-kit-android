package apptentive.com.android.feedback.survey.interaction

import android.text.SpannedString
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import apptentive.com.android.TestCase
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.feedback.EngagementResult
import apptentive.com.android.feedback.MockTimeRule
import apptentive.com.android.feedback.engagement.EngageArgs
import apptentive.com.android.feedback.engagement.EngagementCallback
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.MockEngagementContext
import apptentive.com.android.feedback.engagement.PayloadSenderCallback
import apptentive.com.android.feedback.survey.SurveyModelFactory
import apptentive.com.android.feedback.survey.SurveyModelFactoryProvider
import apptentive.com.android.feedback.survey.model.RenderAs
import apptentive.com.android.feedback.survey.model.SurveyModel
import apptentive.com.android.feedback.survey.model.SurveyQuestionSet
import apptentive.com.android.feedback.survey.model.createMultiChoiceQuestionForV12
import apptentive.com.android.feedback.survey.model.createRangeQuestionForV12
import apptentive.com.android.feedback.survey.model.createSingleLineQuestionForV12
import apptentive.com.android.feedback.survey.utils.createSurveyViewModel
import apptentive.com.android.toProperJson
import io.mockk.every
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test

class SurveyInteractionLauncherTest : TestCase() {

    @get:Rule
    val timeRule = MockTimeRule(currentTime = 1000.0, utcOffset = -18000)

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Test
    fun testViewModel() {
        val context = createEngagementContext()
        val model = createSurveyModel(
            createSingleLineQuestionForV12(id = "id_1"),
            createRangeQuestionForV12(id = "id_2"),
            createMultiChoiceQuestionForV12(
                id = "id_3",
                answerChoiceConfigs = listOf(
                    mapOf("id" to "choice_1", "value" to "value", "type" to "select_option", "title" to "Title 1"),
                    mapOf("id" to "choice_2", "type" to "select_other", "title" to "Title 2", "value" to "value")
                ),
                minSelections = 1,
                maxSelections = 2
            )
        )

        val surveyModelFactory = object : SurveyModelFactory {
            override fun getSurveyModel(): SurveyModel {
                return model
            }
        }

        val surveyModelFactoryProviderMock = mockk<SurveyModelFactoryProvider>()
        every { surveyModelFactoryProviderMock.get() } returns surveyModelFactory

        DependencyProvider.register(surveyModelFactoryProviderMock)

        val viewModel = createSurveyViewModel(context)

        viewModel.submitListSurvey()

        assertResults(
            // survey response payload
            toProperJson(
                "{'response':{'id':'interaction_id','answers':{'id_1':{'state':'empty'},'id_2':{'state':'empty'},'id_3':{'state':'empty'}},'session_id':'xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx','client_created_at':1000.0,'client_created_at_utc_offset':-18000,'nonce':'xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx'}}"
            ),

            // "submit" event
            EngageArgs(
                event = Event.internal(name = "submit", interaction = "Survey"),
                interactionId = "interaction_id"
            ),

            // "close" event
            EngageArgs(
                event = Event.internal(name = "close", interaction = "Survey"),
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
            EngagementResult.InteractionNotShown("No runnable interactions")
        },
        onSendPayload = onSendPayload ?: { payload ->
            addResult(payload.toJson())
        }
    )

    private fun createSurveyModel(vararg questionSet: SurveyQuestionSet): SurveyModel {
        return SurveyModel(
            interactionId = "interaction_id",
            questionSet = questionSet.toList(),
            name = "name",
            surveyIntroduction = "description",
            submitText = "submitText",
            requiredText = "requiredText",
            validationError = null,
            showSuccessMessage = false,
            successMessage = "successMessage",
            closeConfirmTitle = "Close survey?",
            closeConfirmMessage = "All the changes will be lost",
            closeConfirmCloseText = "close",
            closeConfirmBackText = "Back to survey",
            termsAndConditionsLinkText = SpannedString("Terms & Conditions"),
            disclaimerText = "Disclaimer text",
            introButtonText = "START",
            renderAs = RenderAs.LIST,
            successButtonText = "THANK YOU"
        )
    }
}
