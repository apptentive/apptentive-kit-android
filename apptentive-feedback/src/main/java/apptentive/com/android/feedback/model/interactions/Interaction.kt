package apptentive.com.android.feedback.model.interactions

data class Interaction(
    val id: String,
    val type: String,
    val priority: Int?,
    val configuration: Map<String, *>,
    val version: String,
    val displayType: String? = null
) {
    companion object {
        fun fromJson(json: Map<String, *>): Interaction {
            return Interaction(
                id = json["id"] as String,
                type = json["type"] as String,
                priority = json["priority"] as? Int,
                configuration = json["configuration"] as Map<String, *>,
                version = json["version"].toString(),
                displayType = json["display_type"] as? String
            )
        }
    }
}