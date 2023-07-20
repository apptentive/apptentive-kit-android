package apptentive.com.android.feedback.messagecenter.viewmodel

import android.text.format.DateUtils.DAY_IN_MILLIS
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import apptentive.com.android.TestCase
import apptentive.com.android.concurrent.Executor
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.core.isInThePast
import apptentive.com.android.core.toSeconds
import apptentive.com.android.feedback.EngagementResult
import apptentive.com.android.feedback.backend.MessageCenterService
import apptentive.com.android.feedback.dependencyprovider.MessageCenterModelProvider
import apptentive.com.android.feedback.dependencyprovider.createMessageCenterViewModel
import apptentive.com.android.feedback.engagement.EngageArgs
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.MockEngagementContext
import apptentive.com.android.feedback.engagement.MockEngagementContextFactory
import apptentive.com.android.feedback.engagement.interactions.InteractionType
import apptentive.com.android.feedback.message.MessageCenterInteraction
import apptentive.com.android.feedback.message.MessageManager
import apptentive.com.android.feedback.message.MessageManagerFactory
import apptentive.com.android.feedback.message.MessageManagerFactoryProvider
import apptentive.com.android.feedback.message.MessageRepository
import apptentive.com.android.feedback.messagecenter.utils.MessageCenterEvents.EVENT_NAME_ATTACHMENT_DELETE
import apptentive.com.android.feedback.messagecenter.utils.MessageCenterEvents.EVENT_NAME_CLOSE
import apptentive.com.android.feedback.messagecenter.utils.MessageCenterEvents.EVENT_NAME_READ
import apptentive.com.android.feedback.messagecenter.utils.MessageCenterEvents.EVENT_NAME_STATUS
import apptentive.com.android.feedback.messagecenter.view.GreetingData
import apptentive.com.android.feedback.messagecenter.view.ListItemType
import apptentive.com.android.feedback.messagecenter.view.MessageViewData
import apptentive.com.android.feedback.messagecenter.view.ProfileViewData
import apptentive.com.android.feedback.model.Message
import apptentive.com.android.feedback.model.MessageList
import apptentive.com.android.feedback.utils.convertToGroupDate
import apptentive.com.android.util.Result
import apptentive.com.android.util.generateUUID
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

private val attachments = listOf(
    Message.Attachment(
        id = "attachment 1",
        contentType = "jpg",
        size = 100000,
        url = "www.google.com",
        sourceUriOrPath = null,
        localFilePath = "cache://attachment1",
        originalName = "attachment1.jpg"
    ),
    Message.Attachment(
        id = "attachment 2",
        contentType = "png",
        size = 10000,
        url = null,
        sourceUriOrPath = "content://attachment2",
        localFilePath = "cache://attachment2",
        originalName = "attachment2.jpg"
    )
)

val testMessageList: List<Message> = listOf(
    Message(
        id = "Test",
        nonce = "UUID",
        type = "MC",
        body = "Hello",
        sender = null,
        createdAt = toSeconds(System.currentTimeMillis())
    ),
    Message(
        id = "Test2",
        nonce = "UUID2",
        type = "MC2",
        body = "Hello2",
        sender = null,
        createdAt = toSeconds(System.currentTimeMillis() - DAY_IN_MILLIS),
        attachments = attachments
    ),
    Message(
        id = "Test3",
        nonce = "UUID3",
        type = "MC3",
        body = "Hello3",
        sender = null,
        createdAt = toSeconds(System.currentTimeMillis() - (DAY_IN_MILLIS + 100))
    ),
    Message(
        id = "Test4NotRead",
        nonce = "UUID4",
        type = "MC4",
        body = "Hello4",
        sender = null,
        read = false,
        createdAt = toSeconds(System.currentTimeMillis() - (DAY_IN_MILLIS * 7)),
        attachments = attachments.subList(0, 0)
    ),
    Message(
        id = "Test5NotRead",
        nonce = "UUID5",
        type = "MC5",
        body = "Hello5",
        sender = null,
        read = false,
        createdAt = toSeconds(System.currentTimeMillis() - (DAY_IN_MILLIS * 365))
    ),
    Message(
        id = "Test6 Hidden",
        nonce = "UUID6",
        type = "MC6",
        body = "Hello6",
        sender = null,
        createdAt = toSeconds(System.currentTimeMillis() - 1000),
        hidden = true
    )
)

class MessageCenterViewModelTest : TestCase() {

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    private val messageCenterInteractionNoData = MessageCenterInteraction(
        messageCenterId = "12345",
        title = "Message center",
        branding = "Branding",
        composer = MessageCenterInteraction.Composer(
            title = "Composer title",
            hintText = "",
            sendButton = null,
            sendStart = null,
            sendOk = null,
            sendFail = null,
            closeText = "",
            closeBody = "",
            closeDiscard = "",
            closeCancel = ""
        ),
        greeting = MessageCenterInteraction.Greeting("", "", ""),
        status = MessageCenterInteraction.Status(""),
        automatedMessage = MessageCenterInteraction.AutomatedMessage(""),
        errorMessage = null,
        profile = MessageCenterInteraction.Profile(request = true, require = true, null, null)
    )

    private val messageCenterInteractionWithData = messageCenterInteractionNoData.copy(
        greeting = MessageCenterInteraction.Greeting(
            "Title",
            "Greeting message",
            ""
        ), // No url because can't test
        status = MessageCenterInteraction.Status("Status message"),
        automatedMessage = MessageCenterInteraction.AutomatedMessage("Automated message"),
        profile = MessageCenterInteraction.Profile(
            false, false,
            MessageCenterInteraction.Profile.Initial(
                "Profile title",
                "Name hint",
                "Email hint",
                "Skip button",
                "Save button",
                "Email explain"
            ),
            null
        )
    )

    @Before
    fun setup() {
        DependencyProvider.register(
            MockEngagementContextFactory
            {
                MockEngagementContext(
                    onEngage = { args ->
                        addResult(args)
                        EngagementResult.InteractionNotShown("No runnable interactions")
                    },
                    onSendPayload = {}
                )
            }
        )
        val messageManager = MessageManager(
            "conversationId",
            "token",
            MockMessageCenterService(),
            MockExecutor(),
            MockMessageRepository()
        )
        DependencyProvider.register(MessageCenterModelProvider(messageCenterInteractionNoData))
        DependencyProvider.register(MessageManagerFactoryProvider(messageManager))
    }

    @Test
    fun testBaseInitNoData() {
        val viewModel = createMessageCenterViewModel()

        assertEquals(
            MessageCenterViewModel.ValidationDataModel(),
            viewModel.errorMessagesStream.value
        )
        assertFalse(viewModel.hasAutomatedMessage)
        assertEquals(emptyList<Message>(), viewModel.automatedMessageSubject.value)
        assertNull(viewModel.avatarBitmapStream.value)
    }

    @Test
    fun testInitWithData() {
        DependencyProvider.register(MessageCenterModelProvider(messageCenterInteractionWithData))
        val viewModel = createMessageCenterViewModel()

        val vmAutomatedMessage = viewModel.automatedMessageSubject.value.firstOrNull()
        val vmAutomatedMessageCreated =
            vmAutomatedMessage?.createdAt ?: toSeconds(System.currentTimeMillis())
        val automatedMessage = listOf(
            Message(
                nonce = vmAutomatedMessage?.nonce ?: generateUUID(),
                type = Message.MESSAGE_TYPE_TEXT,
                body = viewModel.messageCenterModel.automatedMessage?.body,
                sender = null,
                messageStatus = Message.Status.Sending,
                automated = true,
                inbound = false,
                createdAt = vmAutomatedMessageCreated,
                groupTimestamp = viewModel.automatedMessageSubject.value[0].groupTimestamp
            )
        )
        assertEquals(
            MessageCenterViewModel.ValidationDataModel(),
            viewModel.errorMessagesStream.value
        )
        assertEquals(automatedMessage, viewModel.automatedMessageSubject.value)
        assertResults(createCall(EVENT_NAME_STATUS, null))
    }

    @Test
    fun testNewMessages() {
        val viewModel = createMessageCenterViewModel()
        val manager = DependencyProvider.of<MessageManagerFactory>().messageManager()
        manager.fetchMessages()
        addResult(viewModel.messages)
        assertResults(
            viewModel.groupMessages(
                testMessageList.filterNot { it.hidden == true }
            )
        )
    }

    @Test
    fun testExitMessageCenter() {
        val viewModel = createMessageCenterViewModel()
        viewModel.exitMessageCenter()
        assertResults(createCall(EVENT_NAME_CLOSE, mapOf("cause" to "menu_item")))
    }

    private fun createCall(codePoint: String, data: Map<String, Any?>?) =
        EngageArgs(
            event = Event.internal(codePoint, interaction = InteractionType.MessageCenter),
            interactionId = "12345",
            data = data
        )

    @Test
    fun testAutomatedMessage() {
        DependencyProvider.register(MessageCenterModelProvider(messageCenterInteractionWithData))
        val viewModel = createMessageCenterViewModel()
        assertTrue(viewModel.messages.last().automated!!)
    }

    @Test
    fun testGetAndGroupMessages() {
        val viewModel = createMessageCenterViewModel()
        val now = convertToGroupDate(toSeconds(System.currentTimeMillis())) // DayOfWeek MM/DD
        val dayAgo =
            convertToGroupDate(toSeconds(System.currentTimeMillis() - (DAY_IN_MILLIS * 1))) // DayOfWeek MM/DD
        val weekAgo =
            convertToGroupDate(toSeconds(System.currentTimeMillis() - (DAY_IN_MILLIS * 7))) // MM/DD
        val yearAgo =
            convertToGroupDate(toSeconds(System.currentTimeMillis() - (DAY_IN_MILLIS * 365))) // MM/DD/YYYY

        viewModel.messages.forEach { assertTrue(isInThePast(it.createdAt)) }
        assertEquals(yearAgo, viewModel.messages[4].groupTimestamp)
        assertEquals(weekAgo, viewModel.messages[3].groupTimestamp)
        assertEquals(dayAgo, viewModel.messages[1].groupTimestamp)
        assertNull(viewModel.messages[2].groupTimestamp) // If same day, don't show group timestamp
        assertEquals(now, viewModel.messages[0].groupTimestamp)
        assertEquals(5, viewModel.messages.size)
    }

    @Test
    fun testMergeMessages() {
        val viewModel = createMessageCenterViewModel()

        val newMessage = Message(
            id = "New ID",
            nonce = "New UUID",
            type = "text",
            body = "Hello New",
            sender = null,
            createdAt = toSeconds(System.currentTimeMillis() - (DAY_IN_MILLIS / 2))
        )

        val duplicateMessage = testMessageList.first()

        assertEquals(7, viewModel.newMessages.value?.size ?: 0)

        val newMessageList = viewModel.mergeMessages(listOf(newMessage, duplicateMessage))

        assertEquals(6, newMessageList.size)
        // new message get to last position since the sorting is only available in the MessageRepository
        assertEquals(newMessage, newMessageList[5]) // Should be second most recent
    }

    @Test
    fun testValidation() {
        var viewModel = createMessageCenterViewModel()
        // Require email is set
        // Empty email
        viewModel.validateMessageWithProfile("Test", "")
        addResult(MessageCenterViewModel.ValidationDataModel(emailError = true))
        assertResults(
            viewModel.errorMessagesStream.value ?: MessageCenterViewModel.ValidationDataModel()
        )

        // Invalid email
        viewModel.validateMessageWithProfile("Test", "test@.com")
        addResult(MessageCenterViewModel.ValidationDataModel(emailError = true))
        assertResults(
            viewModel.errorMessagesStream.value ?: MessageCenterViewModel.ValidationDataModel()
        )

        // Blank message
        viewModel.validateMessageWithProfile("", "test@test.com")
        addResult(MessageCenterViewModel.ValidationDataModel(messageError = true))
        assertResults(
            viewModel.errorMessagesStream.value ?: MessageCenterViewModel.ValidationDataModel()
        )

        DependencyProvider.register(
            MessageCenterModelProvider(
                messageCenterInteractionNoData.copy(
                    profile = MessageCenterInteraction.Profile(
                        request = true,
                        require = false,
                        null,
                        null
                    )
                )
            )
        )
        viewModel = createMessageCenterViewModel()
        // Request email is set
        // Empty email
        viewModel.validateMessageWithProfile("Test", "")
        addResult(MessageCenterViewModel.ValidationDataModel())
        assertResults(
            viewModel.errorMessagesStream.value ?: MessageCenterViewModel.ValidationDataModel()
        )

        // Invalid email
        viewModel.validateMessageWithProfile("Test", "test@.com")
        addResult(MessageCenterViewModel.ValidationDataModel(emailError = true))
        assertResults(
            viewModel.errorMessagesStream.value ?: MessageCenterViewModel.ValidationDataModel()
        )
    }

    @Test
    fun testHandleUnreadMessages() {
        val viewModel = createMessageCenterViewModel()
        val unreadMessages = viewModel.messages.filter { it.read != true }

        assertEquals(2, unreadMessages.size)

        viewModel.handleUnreadMessages()

        assertEquals(0, viewModel.messages.filter { it.read != true }.size)

        assertResults(
            *unreadMessages.map {
                createCall(
                    codePoint = EVENT_NAME_READ,
                    data = mapOf(
                        "message_id" to it.id,
                        "message_type" to it.type
                    )
                )
            }.toTypedArray()
        )
    }

    @Test
    fun testBuildMessageViewDataModel() {
        DependencyProvider.register(MessageCenterModelProvider(messageCenterInteractionWithData))
        val viewModel = createMessageCenterViewModel()

        val expectedList = mutableListOf<MessageViewData>()
        expectedList.add(
            MessageViewData(
                ListItemType.HEADER,
                GreetingData("Title", "Greeting message", null),
                null,
                null
            )
        )
        viewModel.messages.forEach { expectedList.add(MessageViewData(ListItemType.MESSAGE, null, null, it)) }
        expectedList.add(
            MessageViewData(
                ListItemType.FOOTER,
                null,
                ProfileViewData("Email hint", "Name hint", viewModel.shouldCollectProfileData),
                null
            )
        )

        assertEquals(expectedList, viewModel.buildMessageViewDataModel())

        val profileNotVisibleList = expectedList.apply {
            val profileView = last().copy(profileData = last().profileData?.copy(showProfile = false))
            removeLast()
            expectedList.add(profileView)
        }
        assertEquals(profileNotVisibleList, viewModel.buildMessageViewDataModel())
    }

    @Test
    fun testAddAttachments() {
        val viewModel = createMessageCenterViewModel()

        assertNull(viewModel.draftAttachmentsStream.value)

        viewModel.addAttachments(attachments)

        assertEquals(2, viewModel.draftAttachmentsStream.value?.size)
    }

    @Test
    fun testRemoveAttachment() {
        val viewModel = createMessageCenterViewModel()
        viewModel.addAttachments(attachments)

        assertEquals(2, viewModel.draftAttachmentsStream.value?.size)

        viewModel.removeAttachment(attachments.first())
        assertResults(createCall(EVENT_NAME_ATTACHMENT_DELETE, null))

        assertEquals(1, viewModel.draftAttachmentsStream.value?.size)
        assertEquals(attachments[1], viewModel.draftAttachmentsStream.value?.get(0))

        // This shouldn't do anything because we already removed
        viewModel.removeAttachment(attachments.first())
        // Same as before
        assertEquals(1, viewModel.draftAttachmentsStream.value?.size)
        assertEquals(attachments[1], viewModel.draftAttachmentsStream.value?.get(0))
        assertResults(createCall(EVENT_NAME_ATTACHMENT_DELETE, null))

        viewModel.removeAttachment(attachments[1])

        assertEquals(0, viewModel.draftAttachmentsStream.value?.size)
        assertResults(createCall(EVENT_NAME_ATTACHMENT_DELETE, null))
    }

    @Test
    fun testGetFirstUnreadMessagePosition() {
        DependencyProvider.register(MessageCenterModelProvider(messageCenterInteractionWithData))
        val viewModel = createMessageCenterViewModel()

        val firstUnreadItem = viewModel
            .getFirstUnreadMessagePosition(viewModel.buildMessageViewDataModel())

        // Messages are only sorted at the MessageRepository level
        assertEquals(4, firstUnreadItem)
    }
}

class MockMessageCenterService : MessageCenterService {
    override fun getMessages(
        conversationToken: String,
        conversationId: String,
        lastMessageID: String,
        callback: (Result<MessageList>) -> Unit
    ) {
        callback(Result.Success(MessageList(testMessageList, null, null)))
    }

    override fun getAttachment(remoteUrl: String, callback: (Result<ByteArray>) -> Unit) {
        callback(Result.Success(remoteUrl.toByteArray()))
    }
}

class MockExecutor : Executor {
    override fun execute(task: () -> Unit) {
        task()
    }
}

class MockMessageRepository : MessageRepository {
    override fun getLastReceivedMessageIDFromEntries(): String = ""

    override fun addOrUpdateMessages(messages: List<Message>) {}

    override fun getAllMessages(): List<Message> {
        return testMessageList
    }

    override fun saveMessages() {}

    override fun deleteMessage(nonce: String) {}
}
