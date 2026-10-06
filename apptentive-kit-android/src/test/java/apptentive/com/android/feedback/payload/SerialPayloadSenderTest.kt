package apptentive.com.android.feedback.payload

import apptentive.com.android.TestCase
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.core.network.Result
import apptentive.com.android.feedback.conversation.ConversationCredentialProvider
import apptentive.com.android.feedback.conversation.MockConversationCredential
import apptentive.com.android.feedback.model.payloads.EventPayload
import apptentive.com.android.feedback.utils.ThrottleUtils
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

    @Test
    fun testKillSwitchDropsPayloadsWhenSdkDisabled() {
        // Kill switch (PBI-10061 / sdk_enabled=false): while disabled, payloads must be dropped —
        // neither queued nor sent — so nothing leaks to the network, now or on a later cold launch.
        val service = MockPayloadService.success()
        DependencyProvider.register<ConversationCredentialProvider>(MockConversationCredential())
        val credential = DependencyProvider.of<ConversationCredentialProvider>()

        val sender = SerialPayloadSender(
            payloadQueue = MockPayloadQueue(),
            callback = ::payloadCallback
        )
        sender.setPayloadService(service)

        try {
            ThrottleUtils.sdkEnabled = false
            sender.enqueuePayload(createPayload("payload-1"), credential)
            sender.enqueuePayload(createPayload("payload-2"), credential)
            // Nothing sent while disabled.
            assertResults()

            // Re-enabled: dropped payloads are NOT resurrected; only new payloads are sent.
            ThrottleUtils.sdkEnabled = true
            sender.enqueuePayload(createPayload("payload-3"), credential)
            assertResults("success: payload-3")
        } finally {
            ThrottleUtils.sdkEnabled = true
        }
    }

    @Test
    fun testKillSwitchBlocksFlushOfQueuedPayloadOnColdLaunch() {
        // A payload persisted in a previous session must not be flushed on cold launch while the SDK
        // is disabled, even though a fresh sender starts active. It flushes once re-enabled.
        DependencyProvider.register<ConversationCredentialProvider>(MockConversationCredential())
        val credential = DependencyProvider.of<ConversationCredentialProvider>()

        val queue = MockPayloadQueue()
        queue.enqueuePayload(createPayload("payload-1").toPayloadData(credential))

        try {
            ThrottleUtils.sdkEnabled = false
            val sender = SerialPayloadSender(
                payloadQueue = queue,
                callback = ::payloadCallback
            )
            sender.setPayloadService(MockPayloadService.success())
            // Kill switch blocks the cold-launch flush.
            assertResults()

            // Once re-enabled, the queued payload flushes normally.
            ThrottleUtils.sdkEnabled = true
            sender.setPayloadService(MockPayloadService.success())
            assertResults("success: payload-1")
        } finally {
            ThrottleUtils.sdkEnabled = true
        }
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
