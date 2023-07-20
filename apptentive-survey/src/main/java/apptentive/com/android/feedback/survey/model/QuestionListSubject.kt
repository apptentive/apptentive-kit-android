package apptentive.com.android.feedback.survey.model

import androidx.annotation.WorkerThread
import apptentive.com.android.core.BehaviorSubject

/**
 * Reactive stream for storing survey questions with caching
 */
internal class QuestionListSubject(
    questions: List<SurveyQuestion<*>>
) : BehaviorSubject<List<SurveyQuestion<*>>>(emptyList()) {
    // We store a mutable list to avoid extra allocations
    private var cachedList = questions.toMutableList()

    init {
        // set cached list as initial value
        value = cachedList
    }

    fun updateCachedList(questions: List<SurveyQuestion<*>>) {
        cachedList = questions.toMutableList()
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
