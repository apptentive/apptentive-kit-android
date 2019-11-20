package apptentive.com.android.feedback.payload

import apptentive.com.android.feedback.test.TestCase
import apptentive.com.android.util.Result
import org.junit.Test

class SerialPayloadSenderTest : TestCase() {
    @Test
    fun testSendingPayload() {
        val service = MockPayloadService.success()

        val sender = SerialPayloadSender(
            payloadService = service,
            payloadQueue = MockPayloadQueue(),
            callback = ::payloadCallback
        )

        val payload1 = createPayload("payload-1")
        val payload2 = createPayload("payload-2")

        sender.sendPayload(payload1)
        sender.pauseSending()

        sender.sendPayload(payload2)

        assertResults("success: ${payload1.nonce}")

        sender.resumeSending()
        assertResults("success: ${payload2.nonce}")
    }

    @Test
    fun testFailedPayload() {
        val service = MockPayloadService {
            when (it.nonce) {
                "payload-2" -> Result.Error(
                    error = PayloadRejectedException(it)
                )
                else -> Result.Success(it)
            }
        }

        val payload1 = createPayload("payload-1")
        val payload2 = createPayload("payload-2")
        val payload3 = createPayload("payload-3")
        val sender = SerialPayloadSender(
            payloadService = service,
            payloadQueue = MockPayloadQueue(),
            callback = ::payloadCallback
        )
        sender.sendPayload(payload1)
        sender.sendPayload(payload2)
        sender.sendPayload(payload3)

        assertResults(
            "success: ${payload1.nonce}",
            "failure: ${payload2.nonce}",
            "success: ${payload3.nonce}"
        )
    }

    private fun payloadCallback(result: Result<Payload>) {
        when (result) {
            is Result.Success -> addResult("success: ${result.data.nonce}")
            is Result.Error -> {
                val error = result.error
                if (error is PayloadSendException) {
                    addResult("failure: ${error.payload.nonce}")
                } else {
                    throw AssertionError("Unexpected exception: $error")
                }
            }
        }
    }

    private fun createPayload(nonce: String, data: String = "Payload data") = Payload(
        nonce = nonce,
        type = PayloadType.Event,
        mediaType = MediaType.applicationJson,
        data = data.toByteArray()
    )
}

