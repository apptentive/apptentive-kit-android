package apptentive.com.android.feedback.payload

import apptentive.com.android.util.Result

class MockPayloadService(private val sendResult: ((PayloadData) -> Result<PayloadData>)? = null) :
    PayloadService {
    override fun sendPayload(payload: PayloadData, callback: (Result<PayloadData>) -> Unit) {
        val result = sendResult?.invoke(payload) ?: Result.Success(payload)
        callback(result)
    }

    companion object {
        fun success() = MockPayloadService()
    }
}
