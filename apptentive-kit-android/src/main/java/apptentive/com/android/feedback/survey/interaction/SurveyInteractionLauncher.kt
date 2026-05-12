package apptentive.com.android.feedback.survey.interaction

import android.os.Handler
import android.os.Looper
import androidx.annotation.VisibleForTesting
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.core.util.Log
import apptentive.com.android.core.util.LogTags.INTERACTIONS
import apptentive.com.android.feedback.engagement.EngagementContext
import apptentive.com.android.feedback.platform.AndroidViewInteractionLauncher
import apptentive.com.android.feedback.survey.SurveyActivity
import apptentive.com.android.feedback.survey.SurveyModelFactoryProvider
import apptentive.com.android.feedback.utils.saveInteractionBackup
import apptentive.com.android.feedback.utils.saveWhereEventBackup
import apptentive.com.android.ui.startViewModelActivity

@VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
internal class SurveyInteractionLauncher : AndroidViewInteractionLauncher<SurveyInteraction>() {
    override fun launchInteraction(
        engagementContext: EngagementContext,
        interaction: SurveyInteraction,
        whereEvent: String?,
    ) {
        super.launchInteraction(engagementContext, interaction, whereEvent)

        Log.i(INTERACTIONS, "Survey interaction launched with title: ${interaction.name}")
        Log.v(INTERACTIONS, "Survey interaction data: $interaction")

        saveInteractionBackup(interaction)
        whereEvent?.let { event ->
            saveWhereEventBackup(event)
        }

        DependencyProvider.register(SurveyModelFactoryProvider(engagementContext, interaction, whereEvent))

        launcSurveyWithARetry(engagementContext, interaction, 1)
    }

    private fun launcSurveyWithARetry(engagementContext: EngagementContext, interaction: SurveyInteraction, retryCount: Int) {
        engagementContext.executors.main.execute {
            try {
                engagementContext.getAppActivity().startViewModelActivity<SurveyActivity>()
            } catch (e: Exception) {
                if (retryCount > 0) {
                    engagementContext.executors.state.execute {
                        Log.e(INTERACTIONS, "Could not start Survey interaction retrying in 1 second", e)
                        Handler(Looper.getMainLooper()).postDelayed({
                            launcSurveyWithARetry(engagementContext, interaction, retryCount - 1)
                        }, 1000)
                    }
                } else {
                    Log.e(INTERACTIONS, "Could not start Survey interaction after a retry", e)
                }
            }
        }
    }
}
