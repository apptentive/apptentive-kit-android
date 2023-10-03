package apptentive.com.android.feedback.utils

import android.os.Build
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.encryption.EncryptionKey
import apptentive.com.android.feedback.conversation.ConversationMetaData
import apptentive.com.android.feedback.conversation.ConversationRepository
import apptentive.com.android.feedback.conversation.ConversationRoster
import apptentive.com.android.feedback.conversation.ConversationState
import apptentive.com.android.feedback.conversation.MockConversationRepository
import apptentive.com.android.feedback.message.MessageRepository
import apptentive.com.android.feedback.message.MockMessageRepository
import apptentive.com.android.feedback.platform.DefaultStateMachine
import apptentive.com.android.feedback.utils.AndroidSDKVersion.getSDKVersion
import io.mockk.every
import io.mockk.mockkObject
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test

class RosterUtilsTest {

    @Before
    fun setUp() {
        DependencyProvider.register<ConversationRepository>(MockConversationRepository())
        DependencyProvider.register<MessageRepository>(MockMessageRepository())
    }

    @Test
    fun testUpdateRosterForLogout() {
        val conversationRoster = ConversationRoster()
        val activeConversationMetaData = ConversationMetaData(
            ConversationState.LoggedIn("user123", ByteArray(0)),
            "path123"
        )
        conversationRoster.activeConversation = activeConversationMetaData
        DefaultStateMachine.conversationRoster = conversationRoster
        val conversationId = "123"

        RosterUtils.updateRosterForLogout(conversationId)

        assertNull(conversationRoster.activeConversation)
        assertEquals(1, conversationRoster.loggedOut.size)
        val loggedOutMetadata = conversationRoster.loggedOut[0]
        assertTrue(loggedOutMetadata.state is ConversationState.LoggedOut)
        assertEquals(conversationId, (loggedOutMetadata.state as ConversationState.LoggedOut).id)
    }

    @Test
    fun testUpdateRosterForLogin() {
        mockkObject(AndroidSDKVersion)
        every { getSDKVersion() } returns Build.VERSION_CODES.M

        val conversationRoster = ConversationRoster()
        val loggedOutData = ConversationMetaData(
            ConversationState.LoggedOut("user123", "subject123"),
            "path123"
        )
        conversationRoster.loggedOut = listOf(loggedOutData)
        DefaultStateMachine.conversationRoster = conversationRoster
        val subject = "subject123"
        val encryptionKey = EncryptionKey()

        RosterUtils.updateRosterForLogin(subject, encryptionKey, ByteArray(0))

        assertEquals(0, conversationRoster.loggedOut.size)
        val activeConversation = conversationRoster.activeConversation
        assertTrue(activeConversation?.state is ConversationState.LoggedIn)
        assertEquals(subject, (activeConversation?.state as ConversationState.LoggedIn).subject)
    }
}
