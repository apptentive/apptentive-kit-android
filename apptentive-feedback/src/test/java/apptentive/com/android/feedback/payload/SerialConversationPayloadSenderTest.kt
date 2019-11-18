package apptentive.com.android.feedback.payload

import apptentive.com.android.feedback.test.TestCase
import apptentive.com.android.util.Result
import org.junit.Test
import java.lang.AssertionError
import java.util.*

class SerialConversationPayloadSenderTest : TestCase() {
    @Test
    fun testSendingPayload() {
        val service: PayloadService = MockPayloadService()
        val queue: PayloadQueue = MockPayloadQueue()

        val payload = Payload(
            nonce = "1",
            type = Payload.PayloadType.Event,
            mediaType = MediaType.applicationJson,
            data = "This a payload".toByteArray()
        )
        val sender = SerialPayloadSender(payloadService = service, payloadQueue = queue)
        sender.sendPayload(payload, ::payloadCallback)

        assertResults("success: 1")
    }

    private fun payloadCallback(result: Result<Payload>) {
        when (result) {
            is Result.Success -> addResult("success: ${result.data.nonce}")
            is Result.Error -> addResult("failure: ${result.error.message}")
        }
    }
}

private class MockPayloadService : PayloadService {
    override fun sendPayload(payload: Payload, callback: (Result<Payload>) -> Unit) {
        callback(Result.Success(payload))
    }
}

private class MockPayloadQueue : PayloadQueue {
    private val payloads: Queue<Payload> = LinkedList<Payload>()

    override fun enqueuePayload(payload: Payload) {
        payloads.add(payload)
    }

    override fun nextUnsentPayload(): Payload? {
        return if (payloads.isEmpty()) null else payloads.peek()
    }

    override fun deletePayload(payload: Payload) {
        payloads.poll() ?: throw AssertionError("Payload was not in the queue")
    }
}