package apptentive.com.android.feedback.survey.interaction

import apptentive.com.android.feedback.model.InvocationData
import apptentive.com.android.feedback.survey.model.SurveyQuestionSet
import apptentive.com.android.util.MissingKeyException
import apptentive.com.android.util.getList
import apptentive.com.android.util.getMap
import apptentive.com.android.util.getString
import apptentive.com.android.util.optString

internal interface SurveyQuestionSetConverter {
    fun convert(configuration: SurveyQuestionSetConfiguration): SurveyQuestionSet
}

internal class DefaultSurveyQuestionSetConverter : SurveyQuestionSetConverter {

    var isPaged = false
    @Suppress("UNCHECKED_CAST")
    @Throws(MissingKeyException::class)
    override fun convert(configuration: SurveyQuestionSetConfiguration): SurveyQuestionSet {
        return SurveyQuestionSet(
            id = configuration.getString("id"),
            invokes = (configuration.getList("invokes") as List<Map<String, Any?>>).map(::convertInvocation),
            questions = configuration.getList("questions").map {
                it as SurveyQuestionConfiguration
            },
            buttonText = configuration.optString("button_text") ?: if (isPaged) "Next" else "Submit",
            shouldContinue = getInvokeBehavior(configuration.optString("behavior") ?: "end"),
        )
    }

    private fun getInvokeBehavior(value: String): Boolean {
        return value == "continue"
    }

    @Suppress("UNCHECKED_CAST")
    private fun convertInvocation(config: Map<String, Any?>) = InvocationData(
        interactionId = config.getOrElse("next_question_set_id") { "" }.toString(),
        criteria = config.getMap("criteria") as Map<String, Any>
    )
}
