package apptentive.com.android.feedback.model.interactions

class UpgradeMessageInteraction(
    id: String,
    private val appVersion: String,
    private val showAppIcon: Boolean,
    private val showPoweredBy: Boolean,
    private val body: String,
    private val active: Boolean
) : Interaction(id)