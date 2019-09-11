package apptentive.com.android.feedback.model

import apptentive.com.android.feedback.model.interactions.Interaction

class EngagementManifest(
    private val interaction: Map<String, Interaction>,
    private val targets: Targets
) {
}