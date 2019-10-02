package apptentive.com.android.feedback.model.interactions

import java.net.URL

class NavigateToLinkInteraction(
    id: String,
    url: URL,
    target: Target
) : Interaction(id) {
    enum class Target {
        New,
        Self
    }
}