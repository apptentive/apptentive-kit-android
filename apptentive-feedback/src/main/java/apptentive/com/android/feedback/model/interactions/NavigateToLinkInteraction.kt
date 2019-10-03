package apptentive.com.android.feedback.model.interactions

import android.net.Uri
import apptentive.com.android.feedback.CONVERSATION
import apptentive.com.android.util.Log

class NavigateToLinkInteraction(
    id: String,
    url: Uri,
    target: Target
) : Interaction(id) {
    enum class Target {
        New,
        Self;

        companion object {
            fun tryParse(value: String?): Target? {
                try {
                    if (value != null) {
                        return valueOf(value)
                    }
                } catch (e: IllegalArgumentException) {
                    Log.e(CONVERSATION, "Invalid value for 'NavigateToLink' target: $value")
                }
                return null
            }
        }
    }
}