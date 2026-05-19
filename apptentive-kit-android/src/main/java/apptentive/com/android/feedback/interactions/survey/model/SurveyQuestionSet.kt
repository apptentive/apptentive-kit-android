package apptentive.com.android.feedback.interactions.survey.model

import apptentive.com.android.feedback.interactions.survey.interaction.SurveyInteraction
import apptentive.com.android.feedback.interactions.survey.interaction.SurveyQuestionConfiguration
import apptentive.com.android.feedback.model.InvocationData

/**
 * Model class to represent survey question set.
 * @param id a string that uniquely identifies the question set
 * @param invokes [SurveyInteraction.Invocation] object with criteria for the next question ID
 * @param questions list of [SurveyQuestion]
 * @param buttonText button text on the question page
 */

internal data class SurveyQuestionSet(
    val id: String,
    val invokes: List<InvocationData>,
    val questions: List<SurveyQuestionConfiguration>,
    val buttonText: String,
    val shouldContinue: Boolean
)
