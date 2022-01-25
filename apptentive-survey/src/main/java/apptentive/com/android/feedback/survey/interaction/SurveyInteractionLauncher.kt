package apptentive.com.android.feedback.survey.interaction

import androidx.annotation.VisibleForTesting
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.feedback.INTERACTIONS
import apptentive.com.android.feedback.platform.AndroidEngagementContext
import apptentive.com.android.feedback.platform.AndroidViewInteractionLauncher
import apptentive.com.android.feedback.survey.SurveyActivity
import apptentive.com.android.feedback.survey.SurveyModelFactoryProvider
import apptentive.com.android.ui.startViewModelActivity
import apptentive.com.android.util.Log

@VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
class SurveyInteractionLauncher : AndroidViewInteractionLauncher<SurveyInteraction>() {
    override fun launchInteraction(
        context: AndroidEngagementContext,
        interaction: SurveyInteraction
    ) {
        Log.i(INTERACTIONS, "Survey interaction launched with title: ${interaction.name}")
        Log.v(INTERACTIONS, "Survey interaction data: $interaction")

        DependencyProvider.register(SurveyModelFactoryProvider(context, interaction))

        context.executors.main.execute {
            context.androidContext.startViewModelActivity<SurveyActivity>()
        }
    }
}