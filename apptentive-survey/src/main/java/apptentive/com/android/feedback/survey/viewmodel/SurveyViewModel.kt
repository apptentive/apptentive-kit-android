package apptentive.com.android.feedback.survey.viewmodel

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
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
import apptentive.com.android.ui.ApptentiveViewModel
import java.lang.Thread.sleep

typealias SurveySubmitCallback = (Map<String, SurveyQuestionAnswer>) -> Unit
typealias SurveyCancelCallback = () -> Unit
typealias SurveyCancelPartialCallback = () -> Unit
typealias SurveyContinuePartialCallback = () -> Unit


class SurveyViewModel(
    private val model: SurveyModel,
    private val executors: Executors,
    private val onSubmit: SurveySubmitCallback,
    private val onCancel: SurveyCancelCallback,
    private val onClose: SurveyCancelPartialCallback,
    private val onBackToSurvey: SurveyContinuePartialCallback,
    questionListItemFactory: SurveyQuestionListItemFactory = DefaultSurveyQuestionListItemFactory()
) : ApptentiveViewModel() {
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
        questionListItemFactory = questionListItemFactory
    )

    private val requiredTextEvent = LiveEvent<String?>()
    val requiredText: LiveData<String?> = requiredTextEvent

    private val exitEvent = LiveEvent<Boolean>()
    val exitStream: LiveData<Boolean> = exitEvent

    private val showConfirmationEvent = LiveEvent<Boolean>()
    val showConfirmation: LiveData<Boolean> = showConfirmationEvent

    private var submitAttempted: Boolean = false
    private var anyQuestionWasAnswered: Boolean = false

    val title = model.name

    val surveyCancelConfirmationDisplay = with(model) {
        SurveyCancelConfirmationDisplay(closeConfirmTitle,
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
                onSubmit(model.questions
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
                    sleep(1000)
                }

                // tell the activity to finish
                executors.main.execute {
                    exit(showConfirmation = false)
                }
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

                // trigger scrolling to the first invalid question
                firstInvalidQuestionIndexEvent.postValue(model.getFirstInvalidRequiredQuestionIndex())
            }
        }
    }

    fun onBackToSurveyFromConfirmationDialog() {
        onBackToSurvey.invoke()
    }

    @MainThread
    fun exit(showConfirmation: Boolean) {
        if (showConfirmation) {
            //When the consumer uses the X button or the back button,
            // try to show the confirmation dialog if user interacted with the survey
            if (submitAttempted || anyQuestionWasAnswered) {
                //user interacted
                showConfirmationEvent.postValue(true)
            } else {
                // we don't need to show any confirmation as the customer
                // didn't interact
                exitEvent.postValue(true)
                onCancel.invoke()
            }
        } else {
            // we are already in the confirmation dialog, so no need to show confirmation again
            exitEvent.postValue(true)
            onClose.invoke()
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
                    SurveySubmitMessageState(model.validationError,false)
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

data class SurveySubmitMessageState(
    val message: String,
    val isValid: Boolean
)

data class SurveyCancelConfirmationDisplay(
    val title: String?,
    val message: String?,
    val positiveButtonMessage: String?,
    val negativeButtonMessage: String?
)