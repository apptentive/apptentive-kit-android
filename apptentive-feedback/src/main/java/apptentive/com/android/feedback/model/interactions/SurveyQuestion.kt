package apptentive.com.android.feedback.model.interactions

open class SurveyQuestion {
    companion object {
        fun fromJson(json: Map<String, *>) : SurveyQuestion {
            return SurveyQuestion() // FIXME: fix parsing survey questions
        }
    }
}