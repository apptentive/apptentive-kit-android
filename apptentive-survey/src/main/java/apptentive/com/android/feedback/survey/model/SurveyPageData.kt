package apptentive.com.android.feedback.survey.model

import apptentive.com.android.feedback.model.InvocationData

internal data class SurveyPageData(
    val id: String,
    val introductionText: String?,
    val disclaimerText: String?,
    val successText: String?,
    val questions: List<SurveyQuestion<*>>,
    val pageIndicatorValue: Int?,
    val advanceActionLabel: String?,
    val invocations: List<InvocationData>,
) {
    enum class PageIndicatorStatus {
        HIDE,
        SHOW_NO_PROGRESS;

        fun toInt(): Int? {
            return when (this) {
                HIDE -> null
                SHOW_NO_PROGRESS -> -1
            }
        }
    }
}
