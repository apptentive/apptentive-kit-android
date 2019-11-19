package apptentive.com.android.feedback.payload

import apptentive.com.android.network.UnexpectedResponseException
import apptentive.com.android.util.Result

internal class MockPayloadService(private val sendResult: ((Payload) -> Result<Payload>)? = null) :
    PayloadService {
    override fun sendPayload(payload: Payload, callback: (Result<Payload>) -> Unit) {
        val result = sendResult?.invoke(payload) ?: Result.Success(payload)
        callback(result)
    }

    companion object {
        fun success() = MockPayloadService()
        fun failure(statusCode: Int) = MockPayloadService {
            Result.Error(
                UnexpectedResponseException(
                    statusCode = statusCode,
                    statusMessage = "Something went wrong"
                )
            )
        }
    }
}