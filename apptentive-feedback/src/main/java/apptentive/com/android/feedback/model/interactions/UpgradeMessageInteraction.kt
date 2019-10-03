package apptentive.com.android.feedback.model.interactions

class UpgradeMessageInteraction(
    id: String,
    val appVersion: String?,
    val showAppIcon: Boolean,
    val showPoweredBy: Boolean,
    val body: String,
    val active: Boolean
) : Interaction(id)