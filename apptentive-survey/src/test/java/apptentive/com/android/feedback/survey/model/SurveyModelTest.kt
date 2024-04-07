package apptentive.com.android.feedback.survey.model

import android.text.SpannableString
import apptentive.com.android.TestCase
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.feedback.EngagementResult
import apptentive.com.android.feedback.engagement.MockEngagementContext
import apptentive.com.android.feedback.engagement.MockEngagementContextFactory
import apptentive.com.android.feedback.survey.utils.SpannedUtils
import apptentive.com.android.feedback.utils.HtmlWrapper
import apptentive.com.android.feedback.utils.HtmlWrapper.linkifiedHTMLString
import io.mockk.every
import io.mockk.mockkObject
import org.junit.Before
import org.junit.Test

class SurveyModelTest : TestCase() {
    //region Question Stream

    @Before
    fun setup() {
        mockkObject(HtmlWrapper)
        every { HtmlWrapper.toHTMLString(any()) } returns SpannableString("TEST")
        every { linkifiedHTMLString(any()) } returns SpannableString("TEST")
        mockkObject(SpannedUtils)
        every { SpannedUtils.isSpannedNotNullOrEmpty(any()) } returns true
    }
    @Test
    fun testQuestionStream() {
        val model = createSurveyModel(
            createSingleLineQuestionForV12(id = "id_1"),
            createSingleLineQuestionForV12(id = "id_2"),
            createSingleLineQuestionForV12(id = "id_3")
        )
        model.questionListSubject.observe {
            it.forEach { question ->
                addResult(question)
            }
        }

        // the update should be triggered immediately (since it's a behaviour subject)
        assertResults(
            createSingleLineQuestion(id = "id_1"),
            createSingleLineQuestion(id = "id_2"),
            createSingleLineQuestion(id = "id_3")
        )

        // update the second question
        model.updateAnswer(
            questionId = "id_2",
            answer = SingleLineQuestion.Answer("text")
        )

        // should receive the update
        assertResults(
            createSingleLineQuestion(id = "id_1"),
            createSingleLineQuestion(id = "id_2", answer = "text"),
            createSingleLineQuestion(id = "id_3")
        )
    }

    //endregion

    @Test
    fun testIntroAndSuccessPage() {
        val surveyModel = createSurveyModel(showSuccessMessage = false, renderAs = RenderAs.PAGED)
        DependencyProvider.register(
            MockEngagementContextFactory
            {
                MockEngagementContext(
                    onEngage = { args ->
                        addResult(args)
                        EngagementResult.InteractionNotShown("No runnable interactions")
                    },
                    onSendPayload = {}
                )
            }
        )

        assert(surveyModel.currentPageID == surveyModel.introPageID)
        assert(surveyModel.getNextQuestionSet() == null)
    }

    @Test
    fun testWithOutSuccessPage() {
        val surveyModel = createSurveyModel(renderAs = RenderAs.PAGED)
        DependencyProvider.register(
            MockEngagementContextFactory
            {
                MockEngagementContext(
                    onEngage = { args ->
                        addResult(args)
                        EngagementResult.InteractionNotShown("No runnable interactions")
                    },
                    onSendPayload = {}
                )
            }
        )

        assert(surveyModel.currentPageID == surveyModel.introPageID)
        assert(surveyModel.getNextQuestionSet() == surveyModel.successPageID)
        surveyModel.goToNextPage()
        assert(surveyModel.currentPageID == surveyModel.successPageID)
    }
}

internal fun createSurveyModel(
    vararg questionSet: SurveyQuestionSet,
    renderAs: RenderAs = RenderAs.LIST,
    showSuccessMessage: Boolean = true,
    surveyIntroduction: String? = "description"
) = SurveyModel(
    interactionId = "interaction_id",
    questionSet = questionSet.toList(),
    name = "name",
    surveyIntroduction = surveyIntroduction,
    submitText = "submitText",
    requiredText = "requiredText",
    validationError = "Validation error",
    showSuccessMessage = showSuccessMessage,
    successMessage = "successMessage",
    closeConfirmTitle = "Close survey?",
    closeConfirmMessage = "All the changes will be lost",
    closeConfirmCloseText = "close",
    closeConfirmBackText = "Back to survey",
    termsAndConditionsLinkText = linkifiedHTMLString("Terms & Conditions"),
    disclaimerText = "Disclaimer text",
    introButtonText = "START",
    successButtonText = "THANK YOU",
    renderAs = renderAs
)
