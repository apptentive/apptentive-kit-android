package apptentive.com.android.feedback.model.payloads

import apptentive.com.android.core.TimeInterval
import apptentive.com.android.core.getTimeSeconds
import apptentive.com.android.core.getUtcOffset

abstract class ConversationPayload(
    nonce: String,
    val clientCreatedAt: TimeInterval = getTimeSeconds(),
    val clientCreatedAtUtcOffset: Int = getUtcOffset()
) : Payload(nonce = nonce) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ConversationPayload) return false
        if (!super.equals(other)) return false

        if (clientCreatedAt != other.clientCreatedAt) return false
        if (clientCreatedAtUtcOffset != other.clientCreatedAtUtcOffset) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + clientCreatedAt.hashCode()
        result = 31 * result + clientCreatedAtUtcOffset
        return result
    }
}
