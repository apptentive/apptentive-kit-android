package apptentive.com.android.feedback.engagement.interactions

abstract class Interaction(val id: String) {
    override fun toString(): String {
        return "${javaClass.simpleName}(id=$id)"
    }
}