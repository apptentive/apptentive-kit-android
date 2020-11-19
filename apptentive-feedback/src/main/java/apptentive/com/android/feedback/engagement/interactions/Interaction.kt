package apptentive.com.android.feedback.engagement.interactions

typealias InteractionId = String

abstract class Interaction(val id: InteractionId, val type: String) {
    override fun toString(): String {
        return "${javaClass.simpleName}(id=$id, type=$type)"
    }
}