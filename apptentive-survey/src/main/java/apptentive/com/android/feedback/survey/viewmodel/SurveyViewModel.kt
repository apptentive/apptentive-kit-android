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
import apptentive.com.android.feedback.survey.model.SingleLineQuestion
import apptentive.com.android.feedback.survey.model.SurveyModel
import apptentive.com.android.feedback.survey.model.SurveyQuestion
import apptentive.com.android.feedback.survey.model.SurveyQuestionAnswer
import apptentive.com.android.feedback.survey.model.update

/**
 * ViewModel for Surveys
 *
 * SurveyViewModel class that is responsible for preparing and managing survey data
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

class SurveyViewModel(
    private val model: SurveyModel,
    private val executors: Executors,
    private val onSubmit: SurveySubmitCallback,
    private val onCancel: SurveyCancelCallback,
    private val onCancelPartial: SurveyCancelPartialCallback,
    private val onClose: SurveyCloseCallback,
    private val onBackToSurvey: SurveyContinuePartialCallback,
) : ViewModel() {
    /** LiveData which transforms a list of {SurveyQuestion} into a list of {SurveyQuestionListItem} */
    private val questionsStream: LiveData<List<SurveyQuestion<*>>> =
        model.questionsStream.asLiveData()

    /** Holds an index to the first invalid question (or -1 if all questions are valid) */
    private val firstInvalidQuestionIndexEvent = LiveEvent<Int>()
    val firstInvalidQuestionIndex: LiveData<Int> = firstInvalidQuestionIndexEvent

    /** Live data which keeps track of the survey "submit" message (shown under the "submit" button) */
    private val surveySubmitMessageState = MutableLiveData<SurveySubmitMessageState>()

    /** LiveData which holds the current list of SurveyQuestionListItem */
    val listItems: LiveData<List<SurveyListItem>> = createQuestionListLiveData(
        questionListItemFactory = DefaultSurveyQuestionListItemFactory()
    )

    private val requiredTextEvent = LiveEvent<String?>()
    val requiredText: LiveData<String?> = requiredTextEvent

    private val exitEvent = LiveEvent<Boolean>()
    val exitStream: LiveData<Boolean> = exitEvent

    private val showConfirmationEvent = MutableLiveData<Boolean>()
    val showConfirmation: LiveData<Boolean> = showConfirmationEvent

    private var submitAttempted: Boolean = false
    private var anyQuestionWasAnswered: Boolean = false

    val title = model.name
    val termsAndConditions = model.termsAndConditionsLinkText

    val surveyCancelConfirmationDisplay = with(model) {
        SurveyCancelConfirmationDisplay(
            closeConfirmTitle,
            closeConfirmMessage,
            closeConfirmBackText,
            closeConfirmCloseText
        )
    }

    //region Answers

    fun updateAnswer(id: String, value: String) {
        updateModel {
            model.updateAnswer(id, SingleLineQuestion.Answer(value))
            updateQuestionAnsweredFlag(model.hasAnyAnswer)
        }
    }

    fun updateAnswer(id: String, selectedIndex: Int) {
        updateModel {
            model.updateAnswer(id, RangeQuestion.Answer(selectedIndex))
            updateQuestionAnsweredFlag(model.hasAnyAnswer)
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
                updateQuestionAnsweredFlag(model.hasAnyAnswer)
            }
        }
    }

    private fun updateQuestionAnsweredFlag(updated: Boolean) {
        executors.main.execute {
            anyQuestionWasAnswered = anyQuestionWasAnswered || updated
        }
    }

    //endregion

    fun submit() {
        submitAttempted = true

        updateModel {
            if (model.allRequiredAnswersAreValid) {
                onSubmit(
                    model.questions
                        .filter { it.hasValidAnswer } // filter out questions with invalid answers
                        .map { it.id to it.answer } // map question id to its answer
                        .toMap()
                )

                if (!model.successMessage.isNullOrBlank()) {
                    surveySubmitMessageState.postValue(
                        SurveySubmitMessageState(
                            model.successMessage,
                            true
                        )
                    )
                }

                exit(showConfirmation = false, successfulSubmit = true)
            } else {
                // trigger error message
                if (!model.validationError.isNullOrBlank()) {
                    surveySubmitMessageState.postValue(
                        SurveySubmitMessageState(
                            model.validationError,
                            false
                        )
                    )
                }

                // get index of first invalid question (description puts header item before questions)
                var firstInvalidQuestionIndex = model.getFirstInvalidRequiredQuestionIndex()
                if (model.description != null) firstInvalidQuestionIndex++

                // trigger scrolling to the first invalid question
                firstInvalidQuestionIndexEvent.postValue(firstInvalidQuestionIndex)
            }
        }
    }

    fun onBackToSurveyFromConfirmationDialog() {
        executors.state.execute { onBackToSurvey.invoke() }
    }

    @MainThread
    fun exit(showConfirmation: Boolean, successfulSubmit: Boolean = false) {
        if (showConfirmation) {
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
                if (successfulSubmit) onClose.invoke()
                else onCancelPartial.invoke()
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
                // header
                if (!model.description.isNullOrEmpty()) {
                    add(SurveyHeaderListItem(model.description))
                }

                // questions
                addAll(questionsListItems)

                // footer
                add(SurveyFooterListItem(model.submitText, messageState))
            }
        }

        return MediatorLiveData<List<SurveyListItem>>().apply {
            // questions stream
            addSource(questionsStream) { questions ->
                // only show message state if:
                // 1. user pressed submit button
                // 2. model provides a validation error
                // 3. at least one of the required questions is not answered
                val messageState: SurveySubmitMessageState? = if (submitAttempted && model.validationError != null && !model.allRequiredAnswersAreValid) {
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
}

internal data class SurveySubmitMessageState(
    val message: String,
    val isValid: Boolean
)

data class SurveyCancelConfirmationDisplay(
    val title: String?,
    val message: String?,
    val positiveButtonMessage: String?,
    val negativeButtonMessage: String?
)

internal typealias SurveySubmitCallback = (Map<String, SurveyQuestionAnswer>) -> Unit
internal typealias SurveyCancelCallback = () -> Unit
internal typealias SurveyCancelPartialCallback = () -> Unit
internal typealias SurveyCloseCallback = () -> Unit
internal typealias SurveyContinuePartialCallback = () -> Unit
