package apptentive.com.android.feedback.survey.interaction

import androidx.annotation.VisibleForTesting
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.feedback.engagement.EngagementContext
import apptentive.com.android.feedback.platform.AndroidViewInteractionLauncher
import apptentive.com.android.feedback.survey.SurveyActivity
import apptentive.com.android.feedback.survey.SurveyModelFactoryProvider
import apptentive.com.android.feedback.utils.saveInteractionBackup
import apptentive.com.android.ui.startViewModelActivity
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.INTERACTIONS

@VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
internal class SurveyInteractionLauncher : AndroidViewInteractionLauncher<SurveyInteraction>() {
    override fun launchInteraction(
        engagementContext: EngagementContext,
        interaction: SurveyInteraction
    ) {
        super.launchInteraction(engagementContext, interaction)

        Log.i(INTERACTIONS, "Survey interaction launched with title: ${interaction.name}")
        Log.v(INTERACTIONS, "Survey interaction data: $interaction")

        saveInteractionBackup(interaction, engagementContext.getAppActivity())

        DependencyProvider.register(SurveyModelFactoryProvider(engagementContext, interaction))

        engagementContext.executors.main.execute {
            engagementContext.getAppActivity().startViewModelActivity<SurveyActivity>()
        }
    }
}
