package apptentive.com.android.feedback.model

data class Target(val interactionId: String, val criteria: Map<String, *>) {
    companion object {
        fun fromJson(json: Map<String, *>): Target {
            return Target(
                interactionId = json["interaction_id"] as String,
                criteria = json["criteria"] as Map<String, *>
            )
        }
    }
}