package apptentive.com.android.feedback.initiator

import apptentive.com.android.feedback.engagement.EngagementContext
import apptentive.com.android.feedback.platform.AndroidViewInteractionLauncher
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.INITIATOR

internal class InitiatorInteractionLauncher : AndroidViewInteractionLauncher<InitiatorInteraction>() {
    override fun launchInteraction(engagementContext: EngagementContext, interaction: InitiatorInteraction, whereEvent: String?) {
        // DO NOT RECORD WHERE EVENT FOR INITIATOR
        super.launchInteraction(engagementContext, interaction, null)
        Log.d(INITIATOR, "launching digital initiator interaction")
    }
}
