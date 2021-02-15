package apptentive.com.android.feedback.survey.viewmodel

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import apptentive.com.android.concurrent.Executors
import apptentive.com.android.core.LiveEvent
import apptentive.com.android.core.asLiveData
import apptentive.com.android.core.postIfChanged
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

class SurveyViewModel(
    private val model: SurveyModel,
    private val executors: Executors,
    private val onSubmit: SurveySubmitCallback,
    questionListItemFactory: SurveyQuestionListItemFactory = DefaultSurveyQuestionListItemFactory()
) : ApptentiveViewModel() {
    /** LiveData which transforms a list of {SurveyQuestion} into a list of {SurveyQuestionListItem} */
    private val questionsStream: LiveData<List<SurveyQuestion<*>>> =
        model.questionsStream.asLiveData()

    /** Holds an index to the first invalid question (or -1 if all questions are valid) */
    private val firstInvalidQuestionIndexEvent = LiveEvent<Int>()
    val firstInvalidQuestionIndex: LiveData<Int> = firstInvalidQuestionIndexEvent

    /** LiveData which keeps track if "invalid" answers should be displayed */
    private val showInvalidQuestionsStream = MutableLiveData<Boolean>()

    /** LiveData which holds the current list of SurveyQuestionListItem */
    val listItems: LiveData<List<SurveyQuestionListItem>> = createQuestionListLiveData(
        questionsStream,
        showInvalidQuestionsStream,
        questionListItemFactory
    )

    private val requiredTextEvent = LiveEvent<String?>()
    val requiredText: LiveData<String?> = requiredTextEvent

    private val _surveySubmitMessageState = MutableLiveData<SurveySubmitMessageState>()
    val surveySubmitMessageState: LiveData<SurveySubmitMessageState> = _surveySubmitMessageState

    private val exitEvent = LiveEvent<Boolean>()
    val exitStream: LiveData<Boolean> = exitEvent

    private val showConfirmationEvent = LiveEvent<Boolean>()
    val showConfirmation: LiveData<Boolean> = showConfirmationEvent

    private var submitAttempted: Boolean = false
    private var anyQuestionWasAnswered: Boolean = false

    val title = model.name
    val introduction = model.description
    val submitButtonText = model.submitText

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
                    _surveySubmitMessageState.postValue(SurveySubmitMessageState(model.successMessage, true))
                    sleep(1000)
                }

                // tell the activity to finish
                executors.main.execute {
                    exit(showConfirmation = false)
                }
            } else {
                // trigger error message
                if (!model.validationError.isNullOrBlank()) {
                    _surveySubmitMessageState.postValue(SurveySubmitMessageState(model.validationError, false))
                }

                // trigger scrolling to the first invalid question
                firstInvalidQuestionIndexEvent.postValue(model.getFirstInvalidRequiredQuestionIndex())

                // show all invalid questions
                showInvalidQuestionsStream.postIfChanged(true)
            }
        }
    }

    @MainThread
    fun exit(showConfirmation: Boolean) {
        if (showConfirmation && (submitAttempted or anyQuestionWasAnswered)) {
            showConfirmationEvent.postValue(true)
        } else {
            exitEvent.postValue(true)
        }
    }

    //region Helpers

    private fun updateModel(callback: () -> Unit) {
        executors.state.execute(callback)
    }

    //endregion

    companion object {
        private fun createQuestionListLiveData(
            questionsStream: LiveData<List<SurveyQuestion<*>>>,
            showInvalidQuestionsStream: LiveData<Boolean>,
            questionListItemFactory: SurveyQuestionListItemFactory
        ): LiveData<List<SurveyQuestionListItem>> {
            return MediatorLiveData<List<SurveyQuestionListItem>>().apply {
                addSource(showInvalidQuestionsStream) { showInvalidQuestion ->
                    value = (questionsStream.value ?: emptyList()).map { question ->
                        questionListItemFactory.createListItem(
                            question,
                            showInvalidQuestion
                        )
                    }
                }
                addSource(questionsStream) { questions ->
                    value = questions.map { question ->
                        questionListItemFactory.createListItem(
                            question,
                            showInvalidQuestionsStream.value ?: false
                        )
                    }
                }
            }
        }
    }
}

data class SurveySubmitMessageState(
    val message: String,
    val isValid: Boolean
)
