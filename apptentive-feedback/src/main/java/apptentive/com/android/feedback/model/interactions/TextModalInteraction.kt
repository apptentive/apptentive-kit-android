package apptentive.com.android.feedback.model.interactions

class TextModalInteraction(id: String, title: String, body: String, actions: List<TextModalAction>) : Interaction(id) {
}

open class TextModalAction {
    companion object {
        fun fromJson(json: Map<String, *>) : TextModalAction {
            TODO()
        }
    }
}