package apptentive.com.android.feedback.payload

import apptentive.com.android.TestCase
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.feedback.conversation.ConversationCredentialProvider
import apptentive.com.android.feedback.conversation.MockConversationCredential
import apptentive.com.android.feedback.model.payloads.EventPayload
import apptentive.com.android.util.Result
import org.junit.Test

class SerialPayloadSenderTest : TestCase() {
    @Test
    fun testSendingPayload() {
        val service = MockPayloadService.success()

        DependencyProvider.register<ConversationCredentialProvider>(MockConversationCredential())

        val sender = SerialPayloadSender(
            payloadQueue = MockPayloadQueue(),
            callback = ::payloadCallback
        )

        val payload1 = createPayload("payload-1")
        val payload2 = createPayload("payload-2")

        sender.setPayloadService(service)
        sender.enqueuePayload(payload1, DependencyProvider.of<ConversationCredentialProvider>())
        sender.pauseSending()

        sender.enqueuePayload(payload2, DependencyProvider.of<ConversationCredentialProvider>())

        assertResults("success: ${payload1.nonce}")

        sender.resumeSending()
        assertResults("success: ${payload2.nonce}")
    }

    @Test
    fun testFailedPayload() {
        DependencyProvider.register<ConversationCredentialProvider>(MockConversationCredential())

        val payload2 = createPayload("payload-2")

        val service = MockPayloadService {
            when (it.nonce) {
                "payload-2" -> Result.Error(
                    data = payload2.toPayloadData(DependencyProvider.of<ConversationCredentialProvider>()),
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

        val credentialProvider = DependencyProvider.of<ConversationCredentialProvider>()
        sender.enqueuePayload(payload1, credentialProvider)
        sender.enqueuePayload(payload2, credentialProvider)
        sender.enqueuePayload(payload3, credentialProvider)

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
