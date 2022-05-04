package apptentive.com.android.feedback.engagement.interactions

import apptentive.com.android.util.InternalUseOnly

@InternalUseOnly
typealias InteractionId = String

@InternalUseOnly
abstract class Interaction(val id: InteractionId, val type: InteractionType) {
    override fun toString(): String {
        return "${javaClass.simpleName}(id=$id, type=$type)"
    }
}
