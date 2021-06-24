package apptentive.com.android.feedback.survey.model

import androidx.annotation.WorkerThread
import apptentive.com.android.core.BehaviorSubject
import apptentive.com.android.core.Observable

class SurveyModel(
    questions: List<SurveyQuestion<*>>,
    val name: String?,
    val description: String?,
    val submitText: String?,
    val requiredText: String?,
    val validationError: String?,
    val showSuccessMessage: Boolean,
    val successMessage: String?,
    val closeConfirmTitle: String?,
    val closeConfirmMessage: String?,
    val closeConfirmCloseText: String?,
    val closeConfirmBackText: String?
) {
    private val questionsSubject = QuestionListSubject(questions) // BehaviourSubject<List<SurveyQuestion<*>>>
    val questionsStream: Observable<List<SurveyQuestion<*>>> = questionsSubject

    val questions: List<SurveyQuestion<*>> get() = questionsSubject.value

    val allRequiredAnswersAreValid get() = getFirstInvalidRequiredQuestionIndex() == -1

    val hasAnyAnswer get() = questions.any { it.hasAnswer }

    @WorkerThread
    fun <T : SurveyQuestionAnswer> updateAnswer(questionId: String, answer: T) {
        questionsSubject.updateAnswer(questionId, answer)
    }

    @WorkerThread
    fun <T : SurveyQuestion<*>> getQuestion(questionId: String): T {
        val question = questions.find { question ->
            question.id == questionId } ?: throw IllegalArgumentException("Question not found: $questionId")

        @Suppress("UNCHECKED_CAST")
        return question as T
    }

    /** Returns the index of the first REQUIRED invalid question (or -1 if all required questions are valid) */
    fun getFirstInvalidRequiredQuestionIndex(): Int {
        return questions.indexOfFirst { it.isRequired && !it.hasValidAnswer }
    }
}

/**
 * Reactive stream for storing survey questions with caching
 */
private class QuestionListSubject(
    questions: List<SurveyQuestion<*>>
) : BehaviorSubject<List<SurveyQuestion<*>>>(emptyList()) {
    // We store a mutable list to avoid extra allocations
    private val cachedList = questions.toMutableList()

    init {
        // set cached list as initial value
        value = cachedList
    }

    @WorkerThread
    fun <T : SurveyQuestionAnswer> updateAnswer(questionId: String, answer: T) {
        /*
        In the traditional reactive approach every object is readonly
        which means you would need to create a copy of everything what
        changed.

        The code would look like this:

        >   val newList = value.toMutableList()
        >
        >   // find the question index
        >   val index = value.indexOfFirst { it.id == questionId }
        >
        >   // make a copy
        >   newList[index] = value[index].copy(answer = answer)
        >
        >   // let observers know that question list has changed
        >   value = newList

        But in this case we decided to introduce some caching for
        performance reasons (avoid unnecessary allocations). This should be
        fine as long as we only modify the model on a dedicated thread.
        */

        // find the question index
        val index = cachedList.indexOfFirst { it.id == questionId }

        // update question's answer
        @Suppress("UNCHECKED_CAST")
        (cachedList[index] as SurveyQuestion<T>).answer = answer

        // let observers know that question list has changed
        value = cachedList
    }
}