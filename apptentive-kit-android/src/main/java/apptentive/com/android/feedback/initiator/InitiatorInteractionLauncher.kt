package apptentive.com.android.feedback.initiator

import apptentive.com.android.core.util.Log
import apptentive.com.android.core.util.LogTags.INITIATOR
import apptentive.com.android.feedback.engagement.EngagementContext
import apptentive.com.android.feedback.platform.AndroidViewInteractionLauncher

internal class InitiatorInteractionLauncher : AndroidViewInteractionLauncher<InitiatorInteraction>() {
    override fun launchInteraction(engagementContext: EngagementContext, interaction: InitiatorInteraction, whereEvent: String?) {
        // DO NOT RECORD WHERE EVENT FOR INITIATOR
        super.launchInteraction(engagementContext, interaction, null)
        Log.d(INITIATOR, "launching digital initiator interaction")
    }
}
