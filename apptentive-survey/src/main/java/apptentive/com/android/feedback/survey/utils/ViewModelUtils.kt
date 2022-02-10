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
import apptentive.com.android.util.generateUUID

private const val EVENT_SUBMIT = "submit"
private const val EVENT_CANCEL = "cancel"
private const val EVENT_CANCEL_PARTIAL = "cancel_partial"
private const val EVENT_CONTINUE_PARTIAL = "continue_partial"

internal fun createSurveyViewModel(
    /*
      we could probably use the interaction id here but it's safer to pass a random string in
      case if user launches the same interaction twice: for example if a "Take Survey" button is
      pressed multiple times really fast - in that case multiple activities would show up but
      they all will be independent
     */
    interactionId: String = generateUUID(),
    context: EngagementContext = DependencyProvider.of<EngagementContextFactory>().engagementContext(),
    surveyModel: SurveyModel = DependencyProvider.of<SurveyModelFactory>().getSurveyModel(),
) = SurveyViewModel(
    model = surveyModel,
    executors = context.executors,
    onSubmit = { answers ->

        // send response
        context.sendPayload(SurveyResponsePayload.fromAnswers(interactionId, answers))

        // engage 'submit' event
        context.engage(
            event = Event.internal(EVENT_SUBMIT, interaction = InteractionType.Survey),
            interactionId = interactionId
        )
    },
    onCancel = {
        context.engage(
            event = Event.internal(EVENT_CANCEL, interaction = InteractionType.Survey),
            interactionId = interactionId
        )
    },
    onClose = {
        context.engage(
            event = Event.internal(EVENT_CANCEL_PARTIAL, interaction = InteractionType.Survey),
            interactionId = interactionId
        )
    },
    onBackToSurvey = {
        context.engage(
            event = Event.internal(EVENT_CONTINUE_PARTIAL, interaction = InteractionType.Survey),
            interactionId = interactionId
        )
    }
)