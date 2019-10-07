package apptentive.com.android.feedback.model.interactions

import apptentive.com.android.feedback.CONVERSATION
import apptentive.com.android.util.Log
import java.net.URL

class NavigateToLinkInteraction(
    id: String,
    url: URL,
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