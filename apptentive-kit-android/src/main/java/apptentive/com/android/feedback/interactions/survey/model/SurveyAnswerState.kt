package apptentive.com.android.feedback.interactions.survey.model

internal sealed class SurveyAnswerState {
    object Skipped : SurveyAnswerState()
    object Empty : SurveyAnswerState()
    data class Answered(val answer: SurveyQuestionAnswer) : SurveyAnswerState()
}
