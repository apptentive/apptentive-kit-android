package apptentive.com.android.feedback.model.payloads

import apptentive.com.android.core.TimeInterval
import apptentive.com.android.core.getTimeSeconds
import apptentive.com.android.core.getUtcOffset
import apptentive.com.android.feedback.ApptentiveDefaultClient
import apptentive.com.android.util.InternalUseOnly

@InternalUseOnly
abstract class ConversationPayload(
    nonce: String,
    val sessionId: String = ApptentiveDefaultClient.sessionId,
    val clientCreatedAt: TimeInterval = getTimeSeconds(),
    val clientCreatedAtUtcOffset: Int = getUtcOffset()
) : Payload(nonce) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ConversationPayload) return false
        if (!super.equals(other)) return false

        if (sessionId != other.sessionId) return false
        if (clientCreatedAt != other.clientCreatedAt) return false
        if (clientCreatedAtUtcOffset != other.clientCreatedAtUtcOffset) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + sessionId.hashCode()
        result = 31 * result + clientCreatedAt.hashCode()
        result = 31 * result + clientCreatedAtUtcOffset
        return result
    }
}
