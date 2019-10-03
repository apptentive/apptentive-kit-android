package apptentive.com.android.feedback.model

import apptentive.com.android.feedback.utils.getMap
import apptentive.com.android.feedback.utils.getString

data class Target(val interactionId: String, val criteria: Map<String, *>) {
    companion object {
        fun fromJson(json: Map<String, *>): Target {
            return Target(
                interactionId = json.getString("interaction_id"),
                criteria = json.getMap("criteria")
            )
        }
    }
}