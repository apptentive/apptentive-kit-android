package apptentive.com.android.feedback.payload

import androidx.annotation.Keep
import apptentive.com.android.util.Result

@Keep
class PayloadResponse {
}

interface PayloadRequestSender {
    fun sendPayloadRequest(
        payload: Payload,
        conversationId: String,
        conversationToken: String,
        callback: (Result<PayloadResponse>) -> Unit
    )
}

class ConversationPayloadService(
    private val requestSender: PayloadRequestSender,
    private val conversationId: String,
    private val conversationToken: String
) : PayloadService {
    override fun sendPayload(payload: Payload, callback: (Result<Payload>) -> Unit) {
        requestSender.sendPayloadRequest(
            payload = payload,
            conversationId = conversationId,
            conversationToken = conversationToken
        ) { result ->
            when (result) {
                is Result.Success -> callback(Result.Success(payload))
                is Result.Error -> callback(Result.Error(result.error))
            }
        }
    }
}