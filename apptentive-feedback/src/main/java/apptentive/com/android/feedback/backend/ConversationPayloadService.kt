package apptentive.com.android.feedback.backend

import androidx.annotation.Keep
import apptentive.com.android.feedback.payload.PayloadData
import apptentive.com.android.feedback.payload.PayloadSendException
import apptentive.com.android.feedback.payload.PayloadService
import apptentive.com.android.network.SendErrorException
import apptentive.com.android.util.Result

@Keep
internal class PayloadResponse

internal interface PayloadRequestSender {
    fun sendPayloadRequest(
        payload: PayloadData,
        conversationId: String,
        conversationToken: String,
        callback: (Result<PayloadResponse>) -> Unit
    )
}

internal class ConversationPayloadService(
    private val requestSender: PayloadRequestSender,
    private val conversationId: String,
    private val conversationToken: String
) : PayloadService {
    override fun sendPayload(payload: PayloadData, callback: (Result<PayloadData>) -> Unit) {
        requestSender.sendPayloadRequest(
            payload = payload,
            conversationId = conversationId,
            conversationToken = conversationToken
        ) { result ->
            when (result) {
                is Result.Success -> callback(Result.Success(payload))
                is Result.Error -> {
                    when (result.error) {
                        is SendErrorException -> {
                            // Convert to more specific Exception
                            callback(Result.Error(payload, PayloadSendException(payload, cause = result.error)))
                        }
                        else -> {
                            // Unexpected Exception type
                            callback(Result.Error(payload, result.error))
                        }
                    }
                }
            }
        }
    }
}
