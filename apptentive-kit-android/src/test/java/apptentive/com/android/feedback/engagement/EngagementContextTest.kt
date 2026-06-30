package apptentive.com.android.feedback.engagement

import apptentive.com.android.TestCase
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.feedback.conversation.ConversationCredentialProvider
import apptentive.com.android.feedback.conversation.MockConversationCredential
import apptentive.com.android.feedback.model.payloads.EventPayload
import apptentive.com.android.feedback.payload.MockPayloadSender
import apptentive.com.android.feedback.utils.ThrottleUtils
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test

class EngagementContextTest : TestCase() {
    private lateinit var context: MockEngagementContext
    private lateinit var payloadSender: MockPayloadSender

    @Before
    fun setup() {
        DependencyProvider.register<ConversationCredentialProvider>(MockConversationCredential())
        context = MockEngagementContext()
        payloadSender = context.getPayloadSender() as MockPayloadSender
    }

    @After
    fun cleanup() {
        ThrottleUtils.sdkEnabled = true
    }

    @Test
    fun enqueuePayloadWhenSdkEnabled() {
        ThrottleUtils.sdkEnabled = true
        val payload = EventPayload(label = "test_event")

        context.enqueuePayload(payload)

        assertThat(payloadSender.payload).isEqualTo(payload)
    }

    @Test
    fun enqueuePayloadWhenSdkDisabled() {
        ThrottleUtils.sdkEnabled = false
        val payload = EventPayload(label = "test_event")

        context.enqueuePayload(payload)

        assertThat(payloadSender.payload).isNull()
    }
}
