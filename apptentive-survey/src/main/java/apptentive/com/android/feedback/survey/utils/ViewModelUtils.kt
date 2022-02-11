package apptentive.com.android.feedback.survey.utils

import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.feedback.engagement.EngagementContext
import apptentive.com.android.feedback.engagement.EngagementContextFactory
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.interactions.InteractionType
import apptentive.com.android.feedback.survey.SurveyModelFactory
import apptentive.com.android.feedback.survey.model.SurveyModel
import apptentive.com.android.feedback.survey.model.SurveyResponsePayload
import apptentive.com.android.feedback.survey.viewmodel.SurveyViewModel

private const val EVENT_SUBMIT = "submit"
private const val EVENT_CANCEL = "cancel"
private const val EVENT_CANCEL_PARTIAL = "cancel_partial"
private const val EVENT_CLOSE = "close"
private const val EVENT_CONTINUE_PARTIAL = "continue_partial"

internal fun createSurveyViewModel(
    context: EngagementContext = DependencyProvider.of<EngagementContextFactory>().engagementContext(),
    surveyModel: SurveyModel = DependencyProvider.of<SurveyModelFactory>().getSurveyModel(),
) = SurveyViewModel(
    model = surveyModel,
    executors = context.executors,
    onSubmit = { answers ->

        // send response
        context.sendPayload(SurveyResponsePayload.fromAnswers(surveyModel.interactionId, answers))

        // engage 'submit' event
        context.engage(
            event = Event.internal(EVENT_SUBMIT, interaction = InteractionType.Survey),
            interactionId = surveyModel.interactionId
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