package apptentive.com.android.feedback.survey.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import apptentive.com.android.concurrent.Executors
import apptentive.com.android.core.LiveEvent
import apptentive.com.android.core.asLiveData
import apptentive.com.android.core.postIfChanged
import apptentive.com.android.feedback.survey.model.MultiChoiceQuestion
import apptentive.com.android.feedback.survey.model.RangeQuestion
import apptentive.com.android.feedback.survey.model.SurveyModel
import apptentive.com.android.feedback.survey.model.SurveyQuestion
import apptentive.com.android.feedback.survey.model.SingleLineQuestion
import apptentive.com.android.feedback.survey.model.SurveyQuestionAnswer
import apptentive.com.android.feedback.survey.model.update
import apptentive.com.android.ui.ApptentiveViewModel

typealias SurveySubmitCallback = (Map<String, SurveyQuestionAnswer>) -> Unit

class SurveyViewModel(
    private val model: SurveyModel,
    private val executors: Executors,
    private val onSubmit: SurveySubmitCallback,
    questionListItemFactory: SurveyQuestionListItemFactory = DefaultSurveyQuestionListItemFactory()
) : ApptentiveViewModel() {
    /** LiveData which transforms a list of {SurveyQuestion} into a list of {SurveyQuestionListItem} */
    private val questionsStream: LiveData<List<SurveyQuestion<*>>> = model.questionsStream.asLiveData()

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

    private val validationErrorTextEvent = LiveEvent<String?>()
    val validationErrorText: LiveData<String?> = validationErrorTextEvent

    private val exitEvent = LiveEvent<Boolean>()
    val exitStream: LiveData<Boolean> = exitEvent

    //region Answers

    fun updateAnswer(id: String, value: String) {
        updateModel {
            model.updateAnswer(id, SingleLineQuestion.Answer(value))
        }
    }

    fun updateAnswer(id: String, selectedIndex: Int) {
        updateModel {
            model.updateAnswer(id, RangeQuestion.Answer(selectedIndex))
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
            }
        }
    }

    //endregion

    fun submit() {
        updateModel {
            if (model.allRequiredAnswersAreValid) {
                onSubmit(model.questions
                    .filter { it.hasValidAnswer } // filter out questions with invalid answers
                    .map { it.id to it.answer } // map question id to its answer
                    .toMap()
                )

                // tell the activity to finish
                exitEvent.postValue(true)
            } else {
                // trigger error message
                validationErrorTextEvent.postValue(model.validationError)

                // trigger scrolling to the first invalid question
                firstInvalidQuestionIndexEvent.postValue(model.getFirstInvalidRequiredQuestionIndex())

                // show all invalid questions
                showInvalidQuestionsStream.postIfChanged(true)
            }
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
