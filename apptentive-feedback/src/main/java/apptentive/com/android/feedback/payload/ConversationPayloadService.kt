package apptentive.com.android.feedback.payload

import apptentive.com.android.util.Result

class ConversationPayloadService(val conversationId : String, val conversationToken : String) : PayloadService {
    override fun sendPayload(payload: Payload, callback: (Result<Payload>) -> Unit) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}