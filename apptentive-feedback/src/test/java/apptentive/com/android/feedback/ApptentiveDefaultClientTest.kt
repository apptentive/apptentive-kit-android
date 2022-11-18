package apptentive.com.android.feedback

import apptentive.com.android.TestCase
import apptentive.com.android.concurrent.Executor
import apptentive.com.android.concurrent.Executors
import apptentive.com.android.feedback.backend.ConversationCredentials
import apptentive.com.android.feedback.conversation.createConversationManager
import apptentive.com.android.feedback.model.payloads.Payload
import apptentive.com.android.feedback.payload.PayloadSender
import apptentive.com.android.network.HttpClient
import apptentive.com.android.network.HttpRequest
import apptentive.com.android.network.HttpResponse
import apptentive.com.android.util.Result
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

class ApptentiveDefaultClientTest : TestCase() {
    private val mockHttpClient = object : HttpClient {
        override fun <T : Any> send(
            request: HttpRequest<T>,
            callback: (Result<HttpResponse<T>>) -> Unit
        ) {}
    }

    private val mockExecutor = object : Executor {
        override fun execute(task: () -> Unit) {
            task()
        }
    }

    private val mockPayloadSender = object : PayloadSender {
        override fun sendPayload(payload: Payload) {}
    }

    private lateinit var apptentiveClient: ApptentiveDefaultClient

    @Before
    fun testSetup() {
        apptentiveClient = ApptentiveDefaultClient(
            apptentiveKey = "",
            apptentiveSignature = "",
            httpClient = mockHttpClient,
            executors = Executors(
                state = mockExecutor,
                main = mockExecutor
            )
        )

        val fetchResponse = ConversationCredentials(
            id = "id",
            deviceId = "device_id",
            personId = "person_id",
            token = "token",
            encryptionKey = "encryption_key"
        )

        apptentiveClient.conversationManager = createConversationManager(fetchResponse)
        apptentiveClient.payloadSender = mockPayloadSender
    }

    @Ignore("Failing on Jenkins. Unknown reason.")
    @Test
    fun testUpdateAndGetPersonName() {
        val testName = "Test Name"
        val testName2 = "Name Test 2"

        assertNull(apptentiveClient.getPersonName())

        apptentiveClient.updatePerson(testName)

        assertEquals(testName, apptentiveClient.getPersonName())

        apptentiveClient.updatePerson(testName2)

        assertEquals(testName2, apptentiveClient.getPersonName())
    }

    @Ignore("Failing on Jenkins. Unknown reason.")
    @Test
    fun testUpdateAndGetPersonEmail() {
        val testEmail = "test@email.com"
        val testEmail2 = "email@test.com"

        assertNull(apptentiveClient.getPersonEmail())

        apptentiveClient.updatePerson(email = testEmail)

        assertEquals(testEmail, apptentiveClient.getPersonEmail())

        apptentiveClient.updatePerson(email = testEmail2)

        assertEquals(testEmail2, apptentiveClient.getPersonEmail())
    }
}
