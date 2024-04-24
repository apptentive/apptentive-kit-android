package apptentive.com.android.feedback.survey.viewmodel

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import apptentive.com.android.concurrent.Executors
import apptentive.com.android.core.LiveEvent
import apptentive.com.android.core.asLiveData
import apptentive.com.android.feedback.survey.model.MultiChoiceQuestion
import apptentive.com.android.feedback.survey.model.RangeQuestion
import apptentive.com.android.feedback.survey.model.RenderAs
import apptentive.com.android.feedback.survey.model.SingleLineQuestion
import apptentive.com.android.feedback.survey.model.SurveyAnswerState
import apptentive.com.android.feedback.survey.model.SurveyModel
import apptentive.com.android.feedback.survey.model.SurveyQuestion
import apptentive.com.android.feedback.survey.model.update
import apptentive.com.android.feedback.survey.utils.END_OF_QUESTION_SET
import apptentive.com.android.feedback.survey.utils.getValidAnsweredQuestions
import apptentive.com.android.feedback.utils.HtmlWrapper.linkifiedHTMLString
import apptentive.com.android.util.isNotNullOrEmpty

/**
 * ViewModel for Surveys
 *
 * [SurveyViewModel] class that is responsible for preparing and managing survey data
 * for BaseSurveyActivity
 *
 * @property model [SurveyModel] data model that represents the survey
 * @property executors [Executors] executes submitted runnable tasks.
 *
 *  Apptentive uses two executors
 *
 *    * state - For long running/ Async operations
 *    * main  - UI related tasks
 *
 * @property onSubmit [SurveySubmitCallback] callback to be executed when survey is submitted
 * @property onCancel [SurveyCancelCallback] callback to be executed when survey is cancelled
 * @property onCancelPartial [SurveyCancelPartialCallback] callback to be executed when survey is cancelled when partially completed
 * @property onClose [SurveyCloseCallback] callback to be executed when survey is closed
 * @property onBackToSurvey [SurveyContinuePartialCallback] callback to be executed
 * when survey is resumed through after an attempt to close
 */

internal class SurveyViewModel(
    private val model: SurveyModel,
    private val executors: Executors,
    private val onSubmit: SurveySubmitCallback,
    private val recordCurrentAnswer: RecordCurrentAnswerCallback,
    private val resetCurrentAnswer: ResetCurrentAnswerCallback,
    private val onCancel: SurveyCancelCallback,
    private val onCancelPartial: SurveyCancelPartialCallback,
    private val onClose: SurveyCloseCallback,
    private val onBackToSurvey: SurveyContinuePartialCallback,
) : ViewModel() {
    /** LiveData which transforms a list of {SurveyQuestion} into a list of {SurveyQuestionListItem} */
    private val questionsStream: LiveData<List<SurveyQuestion<*>>> =
        model.questionListSubject.asLiveData()

    private val shownQuestions: MutableList<SurveyQuestion<*>> = mutableListOf()

    /** Holds an index to the first invalid question (or -1 if all questions are valid) */
    private val firstInvalidQuestionIndexEvent = LiveEvent<Int>()
    val firstInvalidQuestionIndex: LiveData<Int> = firstInvalidQuestionIndexEvent

    internal val allRequiredAnswersAreValid get() = getFirstInvalidRequiredQuestionIndex() == -1

    private val hasAnyAnswer get() = model.currentQuestions.any { it.hasAnswer }

    /** Live data which keeps track of the survey "submit" message (shown under the "submit" button) */
    private val surveySubmitMessageState = MutableLiveData<SurveySubmitMessageState>()

    /** LiveData which holds the current list of SurveyQuestionListItem */
    val listItems: LiveData<List<SurveyListItem>> = createQuestionListLiveData(
        questionListItemFactory = DefaultSurveyQuestionListItemFactory()
    )

    val currentPage: LiveData<SurveyListItem> = createPageItemLiveData(
        questionListItemFactory = DefaultSurveyQuestionListItemFactory()
    )

    private val advanceButtonTextEvent = LiveEvent<String>()
    val advanceButtonText: LiveData<String> = advanceButtonTextEvent

    private val progressBarNumberEvent = LiveEvent<Int?>()
    val progressBarNumber: LiveData<Int?> = progressBarNumberEvent

    private val exitEvent = LiveEvent<Boolean>()
    val exitStream: LiveData<Boolean> = exitEvent

    private val showConfirmationEvent = LiveEvent<Boolean>()
    val showConfirmation: LiveData<Boolean> = showConfirmationEvent

    private var submitAttempted: Boolean = false
    private var anyQuestionWasAnswered: Boolean = false
    private var surveySubmitted: Boolean = false

    val title = linkifiedHTMLString(model.name)
    val termsAndConditions = model.termsAndConditionsLinkText
    val isPaged = model.renderAs == RenderAs.PAGED
    val pageCount = model.questionSet.size

    val surveyCancelConfirmationDisplay = with(model) {
        SurveyCancelConfirmationDisplay(
            closeConfirmTitle,
            closeConfirmMessage,
            closeConfirmBackText,
            closeConfirmCloseText
        )
    }
    //region Answers

    fun updateAnswer(questionId: String, value: String) {
        val oldAnswer = (model.getQuestion(questionId) as SingleLineQuestion).answer
        val newAnswer = SingleLineQuestion.Answer(value)

        if (oldAnswer != newAnswer) {
            updateModel {
                model.updateAnswer(questionId, SingleLineQuestion.Answer(value))
                updateQuestionAnsweredFlag(hasAnyAnswer)
            }
        }
    }

    fun updateAnswer(questionId: String, selectedIndex: Int) {
        val oldAnswer = (model.getQuestion(questionId) as RangeQuestion).answer
        val newAnswer = RangeQuestion.Answer(selectedIndex)

        if (oldAnswer != newAnswer) {
            updateModel {
                model.updateAnswer(questionId, RangeQuestion.Answer(selectedIndex))
                updateQuestionAnsweredFlag(hasAnyAnswer)
            }
        }
    }

    fun updateAnswer(questionId: String, choiceId: String, selected: Boolean, text: String?) {
        updateModel {
            val question: MultiChoiceQuestion = model.getQuestion(questionId)
            val oldAnswer = question.answer
            val newAnswer = oldAnswer.update(
                choiceId = choiceId,
                isChecked = selected,
                text = text,
                allowMultipleAnswers = question.allowMultipleAnswers
            )
            if (oldAnswer != newAnswer) {
                model.updateAnswer(questionId, newAnswer)
                updateQuestionAnsweredFlag(hasAnyAnswer)
            }
        }
    }

    private fun updateQuestionAnsweredFlag(updated: Boolean) {
        executors.main.execute {
            anyQuestionWasAnswered = anyQuestionWasAnswered || updated
        }
    }

    //endregion

    fun submitListSurvey() {
        submitAttempted = true
        updateModel {
            if (allRequiredAnswersAreValid) {
                submitSurvey()
                showSuccessMessage()
                exit(showConfirmation = false, successfulSubmit = true)
            } else {
                updatePageErrors()
            }
        }
    }

    fun advancePage() {
        updateModel {
            if (allRequiredAnswersAreValid) {
                recordCurrentAnswer()
                when {
                    model.currentPageID == model.successPageID -> {
                        exit(showConfirmation = false, successfulSubmit = true)
                    }
                    isLastQuestionInSurvey() -> {
                        submitSurvey()
                        showSuccessPage()
                    }
                    else -> {
                        shownQuestions.addAll(model.currentQuestions)
                        model.goToNextPage()
                    }
                }
            } else {
                updatePageErrors()
            }
        }
    }

    private fun recordCurrentAnswer() {
        recordCurrentAnswer(
            model.currentQuestions
                .associate { it.id to SurveyAnswerState.Answered(it.answer) }
        )
    }

    private fun resetCurrentAnswer() {
        resetCurrentAnswer(
            model.getAllQuestionsInTheSurvey().associate {
                it.id to SurveyAnswerState.Answered(it.answer)
            }
        )
    }

    private fun isLastQuestionInSurvey() = model.nextQuestionSetId == END_OF_QUESTION_SET || model.nextQuestionSetId == model.successPageID

    private fun showSuccessPage() {
        if (model.nextQuestionSetId == model.successPageID) {
            model.goToNextPage()
        } else {
            exit(showConfirmation = false, successfulSubmit = true)
        }
    }

    private fun updatePageErrors() {
        // trigger error message
        model.validationError?.let { errorMessage ->
            surveySubmitMessageState.postValue(
                SurveySubmitMessageState(
                    errorMessage,
                    false
                )
            )
        }

        // get index of first invalid question (description puts header item before questions)
        var firstInvalidQuestionIndex = getFirstInvalidRequiredQuestionIndex()
        if (model.surveyIntroduction != null && model.renderAs == RenderAs.LIST) firstInvalidQuestionIndex++

        // trigger scrolling to the first invalid question
        firstInvalidQuestionIndexEvent.postValue(firstInvalidQuestionIndex)
    }

    private fun showSuccessMessage() {
        model.getCurrentPage().successText?.let { successMessage ->
            surveySubmitMessageState.postValue(
                SurveySubmitMessageState(
                    successMessage,
                    true
                )
            )
        }
    }

    private fun submitSurvey() {
        if (!surveySubmitted) {
            shownQuestions.addAll(model.currentQuestions)
            val answeredQuestions = getValidAnsweredQuestions(shownQuestions)
            val emptyQuestions = shownQuestions.toSet() - answeredQuestions.toSet()
            val skippedQuestions = model.getAllQuestionsInTheSurvey().filter { q -> shownQuestions.none { s -> q.id == s.id } }
            onSubmit(
                answeredQuestions
                    .associate { it.id to SurveyAnswerState.Answered(it.answer) } +
                    emptyQuestions
                        .associate { it.id to SurveyAnswerState.Empty } +
                    skippedQuestions
                        .associate { it.id to SurveyAnswerState.Skipped }
            )
            surveySubmitted = true
        }
    }

    fun onBackToSurveyFromConfirmationDialog() {
        executors.state.execute { onBackToSurvey.invoke() }
    }

    @MainThread
    fun exit(showConfirmation: Boolean, successfulSubmit: Boolean = false) {
        val isSuccessPage = currentPage.value is SurveySuccessPageItem

        if (showConfirmation && !isSuccessPage) {
            // When the consumer uses the X button or the back button,
            //  try to show the confirmation dialog if user interacted with the survey
            if (submitAttempted || anyQuestionWasAnswered) {
                // user interacted
                showConfirmationEvent.postValue(true)
            } else {
                // we don't need to show any confirmation as the customer didn't interact
                exitEvent.postValue(true)
                executors.state.execute { onCancel.invoke() }
            }
        } else {
            // we are already in the confirmation dialog, so no need to show confirmation again
            exitEvent.postValue(true)
            executors.state.execute {
                if (successfulSubmit || isSuccessPage) onClose.invoke()
                else {
                    resetCurrentAnswer()
                    onCancelPartial.invoke()
                }
            }
        }
    }

    //region Helpers

    private fun updateModel(callback: () -> Unit) {
        executors.state.execute(callback)
    }

    //endregion

    private fun createQuestionListLiveData(
        questionListItemFactory: SurveyQuestionListItemFactory
    ): LiveData<List<SurveyListItem>> {
        fun createListItems(
            questions: List<SurveyQuestion<*>>?,
            messageState: SurveySubmitMessageState?
        ): List<SurveyListItem> {
            val questionList = questions ?: emptyList()
            val showInvalidQuestionsFlag = messageState != null && !messageState.isValid
            val questionsListItems = questionList.map { question ->
                questionListItemFactory.createListItem(
                    question,
                    showInvalidQuestionsFlag
                )
            }
            return mutableListOf<SurveyListItem>().apply {
                val currentPage = model.getCurrentPage()
                // header
                if (currentPage.introductionText?.isNotEmpty() == true) {
                    add(SurveyHeaderListItem(currentPage.introductionText))
                }

                // questions
                addAll(questionsListItems)

                // footer
                add(SurveyFooterListItem(currentPage.advanceActionLabel, currentPage.disclaimerText, messageState))
            }
        }

        return MediatorLiveData<List<SurveyListItem>>().apply {
            // questions stream
            addSource(questionsStream) { questions ->
                // only show message state if:
                // 1. user pressed submit button
                // 2. model provides a validation error
                // 3. at least one of the required questions is not answered
                val messageState: SurveySubmitMessageState? = if (submitAttempted && model.validationError != null && !allRequiredAnswersAreValid) {
                    SurveySubmitMessageState(model.validationError, false)
                } else {
                    null
                }

                value = createListItems(
                    questions,
                    messageState
                )
            }

            // submit message state
            addSource(surveySubmitMessageState) { messageState ->
                value = createListItems(
                    questionsStream.value,
                    messageState
                )
            }
        }
    }

    private fun createPageItemLiveData(
        questionListItemFactory: SurveyQuestionListItemFactory
    ): LiveData<SurveyListItem> {
        fun createPageItem(
            questions: List<SurveyQuestion<*>>?,
            messageState: SurveySubmitMessageState?
        ): SurveyListItem {
            val questionList = questions ?: emptyList()
            val showInvalidQuestionsFlag = messageState != null && !messageState.isValid
            val questionsListItems = questionList.map { question ->
                questionListItemFactory.createListItem(
                    question,
                    showInvalidQuestionsFlag
                )
            }

            val currentPage = model.getCurrentPage()
            advanceButtonTextEvent.value = currentPage.advanceActionLabel.orEmpty()
            progressBarNumberEvent.value = currentPage.pageIndicatorValue

            return when {
                currentPage.successText.isNotNullOrEmpty() -> {
                    SurveySuccessPageItem(
                        currentPage.successText,
                        currentPage.disclaimerText.orEmpty()
                    )
                }

                currentPage.introductionText.isNotNullOrEmpty() || currentPage.disclaimerText.isNotNullOrEmpty() -> {
                    SurveyIntroductionPageItem(
                        currentPage.introductionText.orEmpty(),
                        currentPage.disclaimerText.orEmpty()
                    )
                }

                currentPage.questions.isNotNullOrEmpty() -> questionsListItems.first()

                else -> throw IllegalStateException("Survey page is not valid")
            }
        }

        return MediatorLiveData<SurveyListItem>().apply {
            addSource(questionsStream) { questions ->
                value = createPageItem(questions, null)
            }

            addSource(surveySubmitMessageState) { messageState ->
                value = createPageItem(
                    questionsStream.value,
                    messageState
                )
            }
        }
    }

    /** Returns the index of the first REQUIRED invalid question/NOT REQUIRED but cannot submit
     *  (or -1 if all required questions are valid) */
    internal fun getFirstInvalidRequiredQuestionIndex(): Int {
        return model.currentQuestions.indexOfFirst {
            (it.isRequired && !it.hasValidAnswer) ||
                !it.canSubmitOptionalQuestion
        }
    }
}

internal data class SurveySubmitMessageState(
    val message: String,
    val isValid: Boolean
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SurveySubmitMessageState) return false

        //   if (message != other.message) return false
        if (isValid != other.isValid) return false
        return true
    }
}

data class SurveyCancelConfirmationDisplay(
    val title: String?,
    val message: String?,
    val positiveButtonMessage: String?,
    val negativeButtonMessage: String?
)

internal typealias SurveySubmitCallback = (Map<String, SurveyAnswerState>) -> Unit
internal typealias RecordCurrentAnswerCallback = (Map<String, SurveyAnswerState>) -> Unit
internal typealias ResetCurrentAnswerCallback = (Map<String, SurveyAnswerState>) -> Unit
internal typealias SurveyCancelCallback = () -> Unit
internal typealias SurveyCancelPartialCallback = () -> Unit
internal typealias SurveyCloseCallback = () -> Unit
internal typealias SurveyContinuePartialCallback = () -> Unit
