package apptentive.com.android.feedback.model.interactions

class TextModalInteraction(
    id: String,
    val title: String,
    val body: String,
    val actions: List<TextModalAction>
) : Interaction(id) {
}

open class TextModalAction {
    companion object {
        fun fromJson(json: Map<String, *>): TextModalAction {
            TODO()
        }
    }
}