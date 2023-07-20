package apptentive.com.android.feedback.survey.model

import android.text.Spanned
import androidx.annotation.WorkerThread
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.core.ResettableDelegate
import apptentive.com.android.feedback.engagement.EngagementContext
import apptentive.com.android.feedback.engagement.EngagementContextFactory
import apptentive.com.android.feedback.model.InvocationData
import apptentive.com.android.feedback.survey.interaction.DefaultSurveyQuestionConverter
import apptentive.com.android.feedback.survey.model.SurveyPageData.PageIndicatorStatus
import apptentive.com.android.feedback.survey.utils.END_OF_QUESTION_SET
import apptentive.com.android.feedback.survey.utils.UNSET_QUESTION_SET
import apptentive.com.android.util.isNotNullOrEmpty

internal class SurveyModel(
    val interactionId: String,
    val questionSet: List<SurveyQuestionSet>,
    val name: String?,
    val surveyIntroduction: String?,
    val introButtonText: String?,
    val submitText: String,
    val requiredText: String,
    val validationError: String?,
    var showSuccessMessage: Boolean,
    var successMessage: String?,
    val successButtonText: String?,
    val termsAndConditionsLinkText: Spanned?,
    val disclaimerText: String?,
    val renderAs: RenderAs,
    val closeConfirmTitle: String?,
    val closeConfirmMessage: String?,
    val closeConfirmCloseText: String?,
    val closeConfirmBackText: String?,
) {

    private val pages: MutableMap<String, SurveyPageData> = mutableMapOf()
    internal lateinit var currentPageID: String
    private val singlePageID: String = "single"
    internal val introPageID: String = "intro"
    val successPageID: String = "success"

    init {
        when (renderAs) {
            RenderAs.LIST -> setListSurveyPage()
            RenderAs.PAGED -> {
                setIntroPage()
                setSuccessPage()
                setQuestionsPage()
            }
        }
    }

    val questionListSubject: QuestionListSubject =
        QuestionListSubject(getCurrentPage().questions) // BehaviorSubject
    val currentQuestions: List<SurveyQuestion<*>> get() = questionListSubject.value

    var nextQuestionSetId: String by ResettableDelegate(UNSET_QUESTION_SET) {
        val nextPageID = getNextQuestionSet()
        if (nextPageID.isNullOrEmpty()) END_OF_QUESTION_SET else nextPageID
    }

    fun getCurrentPage(): SurveyPageData {
        return pages[currentPageID] ?: throw IllegalStateException("Current page cannot be null")
    }

    fun getNextQuestionSet(): String? {
        val context: EngagementContext =
            DependencyProvider.of<EngagementContextFactory>().engagementContext()

        val nextQuestionSet = context.getNextQuestionSet(getCurrentPage().invocations)

        return if (nextQuestionSet.isNullOrEmpty()) {
            pages.takeIf { it.contains(successPageID) && currentPageID != successPageID }?.let { successPageID }
        } else {
            nextQuestionSet
        }
    }

    @WorkerThread
    fun goToNextPage() {
        currentPageID = nextQuestionSetId
        nextQuestionSetId = UNSET_QUESTION_SET
        val currentPage = pages[currentPageID]
        if (currentPage != null) {
            questionListSubject.value = currentPage.questions
            questionListSubject.updateCachedList(currentPage.questions)
        }
    }

    @WorkerThread
    fun <T : SurveyQuestionAnswer> updateAnswer(questionId: String, answer: T) {
        questionListSubject.updateAnswer(questionId, answer)
    }

    @WorkerThread
    fun <T : SurveyQuestion<*>> getQuestion(questionId: String): T {
        val question = currentQuestions.find { question ->
            question.id == questionId
        } ?: throw IllegalArgumentException("Question not found: $questionId")

        @Suppress("UNCHECKED_CAST")
        return question as T
    }

    fun getAllQuestionsInTheSurvey(): List<SurveyQuestion<*>> {
        return questionSet.flatMap { questionSet ->
            questionSet.questions.map { config ->
                DefaultSurveyQuestionConverter().convert(
                    config = config,
                    requiredTextMessage = requiredText
                )
            }
        }
    }

    private fun setListSurveyPage() {
        val questions = getAllQuestionsInTheSurvey()
        val singlePage = SurveyPageData(
            singlePageID,
            surveyIntroduction,
            disclaimerText,
            if (showSuccessMessage) successMessage else null,
            questions,
            PageIndicatorStatus.HIDE.toInt(),
            submitText,
            listOf(),
        )
        pages[singlePageID] = singlePage
        currentPageID = singlePageID
    }

    private fun setIntroPage() {
        val firstQuestionSetID = questionSet.firstOrNull()?.id.orEmpty()

        if (surveyIntroduction.isNotNullOrEmpty() || disclaimerText.isNotNullOrEmpty()) {
            val introPage = SurveyPageData(
                introPageID,
                surveyIntroduction,
                disclaimerText,
                null,
                listOf(),
                PageIndicatorStatus.SHOW_NO_PROGRESS.toInt(),
                introButtonText,
                listOf(InvocationData(firstQuestionSetID, mapOf())),
            )
            pages[introPageID] = introPage
            currentPageID = introPageID
        } else {
            currentPageID = firstQuestionSetID
        }
    }

    private fun setSuccessPage() {
        val successMessageDescription = successMessage
        val successButtonText = this.successButtonText

        if (showSuccessMessage &&
            successMessageDescription?.isNotEmpty() == true &&
            successButtonText?.isNotEmpty() == true
        ) {
            val successPage = SurveyPageData(
                successPageID,
                null,
                disclaimerText,
                successMessageDescription,
                listOf(),
                PageIndicatorStatus.HIDE.toInt(),
                successButtonText,
                listOf(),
            )
            pages[successPageID] = successPage
        }
    }

    private fun setQuestionsPage() {
        questionSet.forEachIndexed { index, questionSet ->
            val questions: List<SurveyQuestion<*>> = questionSet.questions.map { config ->
                DefaultSurveyQuestionConverter().convert(
                    config = config,
                    requiredTextMessage = requiredText
                )
            }

            val invocations: List<InvocationData> = questionSet.invokes

            val questionPage = SurveyPageData(
                questionSet.id,
                null,
                null,
                null,
                questions,
                index,
                questionSet.buttonText,
                invocations,
            )
            pages[questionPage.id] = questionPage
        }
    }
}

enum class RenderAs {
    PAGED,
    LIST
}
