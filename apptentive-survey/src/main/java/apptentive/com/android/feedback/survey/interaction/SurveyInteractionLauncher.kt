package apptentive.com.android.feedback.survey.interaction

import androidx.annotation.VisibleForTesting
import apptentive.com.android.feedback.engagement.EngagementContext
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.platform.AndroidEngagementContext
import apptentive.com.android.feedback.platform.AndroidInteractionLauncher
import apptentive.com.android.feedback.survey.R
import apptentive.com.android.feedback.survey.SurveyActivity
import apptentive.com.android.feedback.survey.model.SurveyModel
import apptentive.com.android.feedback.survey.model.SurveyResponsePayload
import apptentive.com.android.feedback.survey.viewmodel.SurveyViewModel
import apptentive.com.android.ui.InteractionViewModelFactoryProvider
import apptentive.com.android.ui.startViewModelActivity
import apptentive.com.android.util.generateUUID

// TODO: UI-tests
class SurveyInteractionLauncher(
    private val questionConverter: SurveyQuestionConverter = DefaultSurveyQuestionConverter()
) : AndroidInteractionLauncher<SurveyInteraction>() {
    override fun launchInteraction(
        context: AndroidEngagementContext,
        interaction: SurveyInteraction
    ) {
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

    private fun createSurveyModel(context: AndroidEngagementContext, interaction: SurveyInteraction) = SurveyModel(
        questions = interaction.questions.map { config ->
            questionConverter.convert(
                config = config,
                requiredTextMessage = interaction.requiredText ?: context.getString(R.string.apptentive_required)
            )
        },
        validationError = interaction.validationError
        // FIXME: pass the rest of the configuration
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
                event = Event.internal(EVENT_SUBMIT, interaction = "Survey"),
                interactionId = interactionId
            )
        }
    )

    companion object {
        private const val EVENT_SUBMIT = "submit"
    }
}