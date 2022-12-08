package apptentive.com.android.feedback.survey.utils

import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.feedback.engagement.EngagementContext
import apptentive.com.android.feedback.engagement.EngagementContextFactory
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.interactions.InteractionResponse
import apptentive.com.android.feedback.engagement.interactions.InteractionType
import apptentive.com.android.feedback.survey.DefaultSurveyModelFactory
import apptentive.com.android.feedback.survey.SurveyModelFactory
import apptentive.com.android.feedback.survey.model.MultiChoiceQuestion
import apptentive.com.android.feedback.survey.model.RangeQuestion
import apptentive.com.android.feedback.survey.model.SingleLineQuestion
import apptentive.com.android.feedback.survey.model.SurveyModel
import apptentive.com.android.feedback.survey.model.SurveyQuestionAnswer
import apptentive.com.android.feedback.survey.model.SurveyResponsePayload
import apptentive.com.android.feedback.survey.viewmodel.SurveyViewModel
import apptentive.com.android.feedback.utils.getInteractionBackup

private const val EVENT_SUBMIT = "submit"
private const val EVENT_CANCEL = "cancel"
private const val EVENT_CANCEL_PARTIAL = "cancel_partial"
private const val EVENT_CLOSE = "close"
private const val EVENT_CONTINUE_PARTIAL = "continue_partial"

internal fun createSurveyViewModel(
    context: EngagementContext = DependencyProvider.of<EngagementContextFactory>().engagementContext()
): SurveyViewModel {
    return try {
        createSurveyViewModel(DependencyProvider.of<SurveyModelFactory>().getSurveyModel(), context)
    } catch (exception: Exception) {
        createSurveyViewModel(
            DefaultSurveyModelFactory(
                context,
                getInteractionBackup(context.getAppActivity())
            ).getSurveyModel(),
            context
        )
    }
}

private fun createSurveyViewModel(
    surveyModel: SurveyModel,
    context: EngagementContext
) = SurveyViewModel(
    model = surveyModel,
    executors = context.executors,
    onSubmit = { answers ->

        // send response
        context.sendPayload(SurveyResponsePayload.fromAnswers(surveyModel.interactionId, answers))

        // engage 'submit' event
        context.engage(
            event = Event.internal(EVENT_SUBMIT, interaction = InteractionType.Survey),
            interactionId = surveyModel.interactionId,
            interactionResponses = mapAnswersToResponses(answers)
        )
    },
    onCancel = {
        context.engage(
            event = Event.internal(EVENT_CANCEL, interaction = InteractionType.Survey),
            interactionId = surveyModel.interactionId
        )
    },
    onCancelPartial = {
        context.engage(
            event = Event.internal(EVENT_CANCEL_PARTIAL, interaction = InteractionType.Survey),
            interactionId = surveyModel.interactionId
        )
    },
    onClose = {
        context.engage(
            event = Event.internal(EVENT_CLOSE, interaction = InteractionType.Survey),
            interactionId = surveyModel.interactionId
        )
    },
    onBackToSurvey = {
        context.engage(
            event = Event.internal(EVENT_CONTINUE_PARTIAL, interaction = InteractionType.Survey),
            interactionId = surveyModel.interactionId
        )
    }
)

private fun mapAnswersToResponses(answers: Map<String, SurveyQuestionAnswer>): Map<String, Set<InteractionResponse>> {
    return answers.map { answer ->
        answer.key to when (answer.value) {
            is MultiChoiceQuestion.Answer -> {
                val responses = answer.value as MultiChoiceQuestion.Answer
                responses.choices.mapNotNull {
                    if (it.checked) {
                        if (it.value != null) InteractionResponse.OtherResponse(it.id, it.value)
                        else InteractionResponse.IdResponse(it.id)
                    } else null
                }.toSet()
            }
            is SingleLineQuestion.Answer -> {
                val response = answer.value as SingleLineQuestion.Answer
                // Should never be null at this point
                response.value?.let { setOf(InteractionResponse.StringResponse(it)) } ?: emptySet()
            }
            is RangeQuestion.Answer -> {
                val response = answer.value as RangeQuestion.Answer
                // Should never be null at this point
                response.selectedIndex?.let { setOf(InteractionResponse.LongResponse(it.toLong())) } ?: emptySet()
            }
            else -> emptySet() // Should not happen
        }
    }.toMap()
}
