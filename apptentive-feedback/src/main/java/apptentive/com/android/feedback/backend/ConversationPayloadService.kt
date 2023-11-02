package apptentive.com.android.feedback.backend

import androidx.annotation.Keep
import apptentive.com.android.feedback.payload.AuthenticationFailureException
import apptentive.com.android.feedback.payload.PayloadData
import apptentive.com.android.feedback.payload.PayloadSendException
import apptentive.com.android.feedback.payload.PayloadService
import apptentive.com.android.feedback.utils.parseJsonField
import apptentive.com.android.network.SendErrorException
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.PAYLOADS
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
    private val requestSender: PayloadRequestSender
) : PayloadService {
    override fun sendPayload(payload: PayloadData, callback: (Result<PayloadData>) -> Unit) {
        val conversationId = payload.conversationId
        val conversationToken = payload.token
        if (conversationId == null || conversationToken == null) {
            callback(Result.Error(payload, PayloadSendException(payload, cause = null)))
        } else {
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
                                if ((result.error as SendErrorException).statusCode == 401) { // TODO add isEncrypted check after payload encryption is done
                                    Log.v(PAYLOADS, "Authentication failed for payload: $payload with error ${result.error}")
                                    callback(
                                        Result.Error(
                                            payload,
                                            AuthenticationFailureException(payload, (result.error as SendErrorException).errorMessage?.parseJsonField("error"), cause = result.error)
                                        )
                                    )
                                } else {
                                    callback(
                                        Result.Error(
                                            payload,
                                            PayloadSendException(payload, cause = result.error)
                                        )
                                    )
                                }
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
}
