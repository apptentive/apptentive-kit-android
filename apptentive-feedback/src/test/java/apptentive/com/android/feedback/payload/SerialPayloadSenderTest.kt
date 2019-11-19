package apptentive.com.android.feedback.payload

import apptentive.com.android.feedback.test.TestCase
import apptentive.com.android.util.Result
import org.junit.Test

class SerialPayloadSenderTest : TestCase() {
    @Test
    fun testSendingPayload() {
        val service = MockPayloadService.success()

        val payload = createPayload("payload-1")
        val sender = SerialPayloadSender(
            payloadService = service,
            payloadQueue = MockPayloadQueue()
        )
        sender.sendPayload(payload, ::payloadCallback)

        assertResults("success: ${payload.nonce}")
    }

    @Test
    fun testFailedPayload() {
        val service = MockPayloadService.failure(statusCode = 401)

        val payload = createPayload("payload-1")
        val sender = SerialPayloadSender(
            payloadService = service,
            payloadQueue = MockPayloadQueue()
        )
        sender.sendPayload(payload, ::payloadCallback)

        assertResults("failure: ${payload.nonce}")
    }

    private fun payloadCallback(result: Result<Payload>) {
        when (result) {
            is Result.Success -> addResult("success: ${result.data.nonce}")
            is Result.Error -> addResult("failure: ${result.error.message}")
        }
    }

    private fun createPayload(nonce: String, data: String = "Payload data") = Payload(
        nonce = nonce,
        type = PayloadType.Event,
        mediaType = MediaType.applicationJson,
        data = data.toByteArray()
    )
}

