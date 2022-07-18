package apptentive.com.android.feedback.payload

import apptentive.com.android.TestCase
import apptentive.com.android.feedback.model.payloads.EventPayload
import apptentive.com.android.util.Result
import org.junit.Test

class SerialPayloadSenderTest : TestCase() {
    @Test
    fun testSendingPayload() {
        val service = MockPayloadService.success()

        val sender = SerialPayloadSender(
            payloadQueue = MockPayloadQueue(),
            callback = ::payloadCallback
        )

        val payload1 = createPayload("payload-1")
        val payload2 = createPayload("payload-2")

        sender.setPayloadService(service)
        sender.sendPayload(payload1)
        sender.pauseSending()

        sender.sendPayload(payload2)

        assertResults("success: ${payload1.nonce}")

        sender.resumeSending()
        assertResults("success: ${payload2.nonce}")
    }

    @Test
    fun testFailedPayload() {
        val payload2 = createPayload("payload-2")

        val service = MockPayloadService {
            when (it.nonce) {
                "payload-2" -> Result.Error(
                    data = payload2.toPayloadData(),
                    error = PayloadSendException(it)
                )
                else -> Result.Success(it)
            }
        }

        val payload1 = createPayload("payload-1")
        val payload3 = createPayload("payload-3")
        val sender = SerialPayloadSender(
            payloadQueue = MockPayloadQueue(),
            callback = ::payloadCallback
        )

        sender.setPayloadService(service)

        sender.sendPayload(payload1)
        sender.sendPayload(payload2)
        sender.sendPayload(payload3)

        assertResults(
            "success: ${payload1.nonce}",
            "failure: ${payload2.nonce}",
            "success: ${payload3.nonce}"
        )
    }

    private fun payloadCallback(result: Result<PayloadData>) {
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

    private fun createPayload(
        nonce: String
    ) = EventPayload(
        nonce = nonce,
        label = "app#local#event"
    )
}
