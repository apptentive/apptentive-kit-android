package apptentive.com.android.feedback.engagement.interactions

typealias InteractionId = String

abstract class Interaction(val id: InteractionId) {
    override fun toString(): String {
        return "${javaClass.simpleName}(id=$id)"
    }
}