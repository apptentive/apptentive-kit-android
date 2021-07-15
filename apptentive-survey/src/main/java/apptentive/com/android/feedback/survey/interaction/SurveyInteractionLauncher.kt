package apptentive.com.android.feedback.survey.interaction

import androidx.annotation.VisibleForTesting
import apptentive.com.android.feedback.INTERACTIONS
import apptentive.com.android.feedback.engagement.EngagementContext
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.interactions.InteractionType
import apptentive.com.android.feedback.platform.AndroidEngagementContext
import apptentive.com.android.feedback.platform.AndroidViewInteractionLauncher
import apptentive.com.android.feedback.survey.R
import apptentive.com.android.feedback.survey.SurveyActivity
import apptentive.com.android.feedback.survey.model.SurveyModel
import apptentive.com.android.feedback.survey.model.SurveyResponsePayload
import apptentive.com.android.feedback.survey.viewmodel.SurveyViewModel
import apptentive.com.android.ui.InteractionViewModelFactoryProvider
import apptentive.com.android.ui.startViewModelActivity
import apptentive.com.android.util.Log
import apptentive.com.android.util.generateUUID

// TODO: UI-tests
class SurveyInteractionLauncher(
    private val questionConverter: SurveyQuestionConverter = DefaultSurveyQuestionConverter()
) : AndroidViewInteractionLauncher<SurveyInteraction>() {
    override fun launchInteraction(
        context: AndroidEngagementContext,
        interaction: SurveyInteraction
    ) {
        Log.i(INTERACTIONS, "Survey interaction launched with title: ${interaction.name}")
        Log.v(INTERACTIONS, "Survey interaction data: $interaction")

        val model = createSurveyModel(context, interaction)

        /*
          we could probably use the interaction's id here but it's safer to pass a random string in
          case if user launches the same interaction twice: for example if a "Take Survey" button is
          pressed multiple times really fast - in that case multiple activities would show up but
          they all will be independent
         */
        val surveyInstanceId = generateUUID()

        // this will inject survey view model into the corresponding activity at runtime
        InteractionViewModelFactoryProvider.registerViewModelFactory(surveyInstanceId) {
            createSurveyViewModel(context, model, interaction.id)
        }

        context.executors.main.execute {
            context.androidContext.startViewModelActivity<SurveyActivity>(surveyInstanceId)
        }
    }

    private fun createSurveyModel(
        context: AndroidEngagementContext,
        interaction: SurveyInteraction
    ) = SurveyModel(
        questions = interaction.questions.map { config ->
            questionConverter.convert(
                config = config,
                requiredTextMessage = interaction.requiredText
                    ?: context.getString(R.string.apptentive_required)
            )
        },
        name = interaction.name,
        description = interaction.description,
        submitText = interaction.submitText,
        requiredText = interaction.requiredText,
        validationError = interaction.validationError,
        showSuccessMessage = interaction.showSuccessMessage,
        successMessage = interaction.successMessage,
        closeConfirmTitle = interaction.closeConfirmTitle,
        closeConfirmMessage = interaction.closeConfirmMessage,
        closeConfirmCloseText = interaction.closeConfirmCloseText,
        closeConfirmBackText = interaction.closeConfirmBackText
    )

    @VisibleForTesting
    internal fun createSurveyViewModel(
        context: EngagementContext,
        model: SurveyModel,
        interactionId: String
    ) = SurveyViewModel(
        model = model,
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

    companion object {
        private const val EVENT_SUBMIT = "submit"
        private const val EVENT_CANCEL = "cancel"
        private const val EVENT_CANCEL_PARTIAL = "cancel_partial"
        private const val EVENT_CONTINUE_PARTIAL = "continue_partial"
    }
}