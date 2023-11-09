package apptentive.com.android.feedback.payload

import android.content.Context
import android.content.res.AssetManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import apptentive.com.android.TestCase
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.core.Logger
import apptentive.com.android.debug.Assert.assertEqual
import apptentive.com.android.encryption.AESEncryption23
import apptentive.com.android.encryption.EncryptionFactory
import apptentive.com.android.encryption.NotEncrypted
import apptentive.com.android.feedback.MockAndroidLoggerProvider
import apptentive.com.android.feedback.conversation.MockConversationCredential
import apptentive.com.android.feedback.conversation.MockEncryptedConversationCredential
import apptentive.com.android.feedback.conversation.MockUpdatedConversationCredential
import apptentive.com.android.feedback.conversation.MockUpdatedEncryptedConversationCredential
import apptentive.com.android.feedback.engagement.util.MockAndroidSharedPrefDataStore
import apptentive.com.android.feedback.model.Message
import apptentive.com.android.feedback.model.payloads.EventPayload
import apptentive.com.android.feedback.model.payloads.MessagePayload
import apptentive.com.android.feedback.utils.MultipartParser
import apptentive.com.android.network.HttpMethod
import apptentive.com.android.platform.AndroidSharedPrefDataStore
import apptentive.com.android.serialization.json.JsonConverter
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class PayloadSQLiteHelperTest : TestCase() {
    private lateinit var dbHelper: PayloadSQLiteHelper

    private val context: Context get() = ApplicationProvider.getApplicationContext<Context>()

    @Before
    fun setupDb() {
        DependencyProvider.register<AndroidSharedPrefDataStore>(MockAndroidSharedPrefDataStore())
        dbHelper = PayloadSQLiteHelper(
            context,
            EncryptionFactory.getEncryption(
                shouldEncryptStorage = false,
                oldEncryptionSetting = NotEncrypted
            )
        )
        dbHelper.deleteDatabase(context)
    }

    @After
    fun closeDb() {
        dbHelper.close()
    }

    @Test
    fun addingAndRemovingPayloads() {
        val actual1 = createPayload(
            nonce = "nonce-1",
            type = PayloadType.Event,
            path = ":conversation_id/events",
            method = HttpMethod.POST,
            mediaType = MediaType.applicationJson,
            data = "payload-1"
        )
        val actual2 = createPayload(
            nonce = "nonce-2",
            type = PayloadType.Person,
            path = ":conversation_id/person",
            method = HttpMethod.PUT,
            mediaType = MediaType.applicationJson,
            data = "payload-2"
        )
        dbHelper.addPayload(actual1)
        dbHelper.addPayload(actual2)

        assertEqual(actual1, dbHelper.nextUnsentPayload())
        assertEqual(actual1, dbHelper.nextUnsentPayload())

        dbHelper.deletePayload(actual1.nonce)

        assertEqual(actual2, dbHelper.nextUnsentPayload())
        assertEqual(actual2, dbHelper.nextUnsentPayload())

        dbHelper.deletePayload(actual2.nonce)

        assertNull(dbHelper.nextUnsentPayload())
    }

    @Test
    fun testCorruptedPayloads() {
        val actual1 = createPayload(
            nonce = "nonce-1",
            type = PayloadType.Event,
            path = ":conversation_id/events",
            method = HttpMethod.POST,
            mediaType = MediaType.applicationJson,
            data = "payload-1"
        )
        val actual2 = createPayload(
            nonce = "nonce-2",
            type = PayloadType.Device,
            path = ":conversation_id/device",
            method = HttpMethod.PUT,
            mediaType = MediaType.applicationJson,
            data = "payload-2"
        )
        dbHelper.addPayload(actual1)
        dbHelper.addPayload(actual2)

        assertEqual(actual1, dbHelper.nextUnsentPayload())

        dbHelper.updatePayload("nonce-1", "MyPayloadType")
        assertEqual(actual2, dbHelper.nextUnsentPayload())
    }

    @Test
    fun testUpdateTokens() {
        DependencyProvider.register<Logger>(MockAndroidLoggerProvider())

        val dogImageTempPath = testImageFilePath()
        val attachments = listOf(
            Message.Attachment(
                "1", "image/jpeg",
                localFilePath = dogImageTempPath,
                originalName = "dog.jpg"
            )
        )

        val eventPayload1 = EventPayload(nonce = "event1", label = "test")
        val eventPayloadData1 = eventPayload1.toPayloadData(MockConversationCredential())
        dbHelper.addPayload(eventPayloadData1)

        val messagePayload1 = MessagePayload("message1", attachments, "test", false, false)
        val messagePayloadData1 = messagePayload1.toPayloadData(MockConversationCredential())
        dbHelper.addPayload(messagePayloadData1)

        val eventPayload2 = EventPayload(nonce = "event2", label = "test-encrypted")
        val eventPayloadData2 = eventPayload2.toPayloadData(MockEncryptedConversationCredential())
        dbHelper.addPayload(eventPayloadData2)

        val messagePayload2 = MessagePayload("message2", attachments, "test-encrypted", false, false)
        val messagePayloadData2 = messagePayload2.toPayloadData(MockEncryptedConversationCredential())
        dbHelper.addPayload(messagePayloadData2)

        val imageFile = File(context.cacheDir, dogImageTempPath)
        imageFile.delete()

        dbHelper.updateCredential(MockUpdatedConversationCredential(), "mockedConversationPath")

        val payloadsUpdatedWithUnencryptedToken = dbHelper.readPayloads()

        assertEqual("mockedUpdatedConversationToken", payloadsUpdatedWithUnencryptedToken[0].token)
        assertEqual("mockedUpdatedConversationToken", payloadsUpdatedWithUnencryptedToken[1].token)
        assertEqual("embedded", payloadsUpdatedWithUnencryptedToken[2].token)
        assertEqual("embedded", payloadsUpdatedWithUnencryptedToken[3].token)

        verifySinglePartWithToken(payloadsUpdatedWithUnencryptedToken[2].data, "mockedEncryptedConversationToken")
        verifyMultipartWithToken(payloadsUpdatedWithUnencryptedToken[3].data, "mockedEncryptedConversationToken")

        dbHelper.updateCredential(MockUpdatedEncryptedConversationCredential(), "mockedEncryptedConversationPath")

        val payloadsUpdatedWithEncryptedToken = dbHelper.readPayloads()

        assertEqual("mockedUpdatedConversationToken", payloadsUpdatedWithEncryptedToken[0].token)
        assertEqual("mockedUpdatedConversationToken", payloadsUpdatedWithEncryptedToken[1].token)
        assertEqual("embedded", payloadsUpdatedWithEncryptedToken[2].token)
        assertEqual("embedded", payloadsUpdatedWithEncryptedToken[3].token)

        val decrypted = AESEncryption23(MockEncryptedConversationCredential().payloadEncryptionKey!!).decryptPayloadData(payloadsUpdatedWithEncryptedToken[2].data)

        verifySinglePartWithToken(payloadsUpdatedWithEncryptedToken[2].data, "mockedUpdatedConversationToken")
        verifyMultipartWithToken(payloadsUpdatedWithEncryptedToken[3].data, "mockedUpdatedConversationToken")

        deleteTestImageTempFile()
    }

    private fun verifySinglePartWithToken(data: ByteArray, token: String) {
        val decryptedContent = AESEncryption23(MockEncryptedConversationCredential().payloadEncryptionKey!!).decryptPayloadData(data)
        val json = JsonConverter.toMap(String(decryptedContent, Charsets.UTF_8))
        val nestedJson = json["event"] as Map<String, Any>

        Assert.assertEquals("xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx", nestedJson["session_id"])
        Assert.assertTrue(1698774495.52 < nestedJson["client_created_at"] as Double)
        Assert.assertEquals("test-encrypted", nestedJson["label"])
        Assert.assertEquals("event2", nestedJson["nonce"])
        Assert.assertEquals(token, json["token"])
    }

    private fun verifyMultipartWithToken(data: ByteArray, token: String) {
        val inputStream = ByteArrayInputStream(data)
        val parser = MultipartParser(inputStream, "s16u0iwtqlokf4v9cpgne8a2amdrxz735hjby")

        Assert.assertEquals(2, parser.numberOfParts)

        val firstPart = parser.getPartAtIndex(0)!!

        Assert.assertEquals(
            "Content-Disposition: form-data;name=\"message\"\r\n" +
                "Content-Type: application/octet-stream\r\n",
            firstPart.multipartHeaders
        )

        val decryptedContent = AESEncryption23(MockEncryptedConversationCredential().payloadEncryptionKey!!).decryptPayloadData(firstPart.content)
        val decryptedPart = MultipartParser.parsePart(ByteArrayInputStream(decryptedContent), 0L..decryptedContent.size + 2) // TODO: Why do we have to add 2 here?

        Assert.assertEquals(
            "Content-Disposition: form-data;name=\"message\"\r\n" +
                "Content-Type: application/json;charset=UTF-8\r\n",
            decryptedPart!!.multipartHeaders
        )

        val json = JsonConverter.toMap(String(decryptedPart.content, Charsets.UTF_8))
        Assert.assertEquals("xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx", json["session_id"])
        Assert.assertTrue(1698774495.52 < json["client_created_at"] as Double)
        Assert.assertEquals("test-encrypted", json["body"])
        Assert.assertEquals("message2", json["nonce"])
        Assert.assertEquals(token, json["token"])

        val secondPart = parser.getPartAtIndex(1)!!

        Assert.assertEquals(
            "Content-Disposition: form-data;name=\"file[]\";filename=\"dog.jpg\"\r\n" +
                "Content-Type: application/octet-stream\r\n",
            secondPart.multipartHeaders
        )

        val decryptedContent2 = AESEncryption23(MockEncryptedConversationCredential().payloadEncryptionKey!!).decryptPayloadData(secondPart.content)
        val decryptedPart2 = MultipartParser.parsePart(ByteArrayInputStream(decryptedContent2), 0L..decryptedContent2.size + 2) // TODO: Why do we have to add 2 here?

        Assert.assertEquals(
            "Content-Disposition: form-data;name=\"file[]\";filename=\"dog.jpg\"\r\n" +
                "Content-Type: image/jpeg\r\n",
            decryptedPart2!!.multipartHeaders
        )

        Assert.assertTrue(decryptedPart2.content.isNotEmpty())
    }

    private fun testImageFilePath(): String {
        val context: Context = InstrumentationRegistry.getInstrumentation().context
        val assetManager: AssetManager = context.assets
        val originalAssetName = "dog.jpg" // Replace with the name of your image
        val outputFile = File(context.cacheDir, originalAssetName)

        try {
            val inputStream: InputStream = assetManager.open(originalAssetName)
            val outputStream = FileOutputStream(outputFile)
            val buffer = ByteArray(1024)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
            inputStream.close()
            outputStream.close()

            return outputFile.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            return ""
        }
    }

    private fun deleteTestImageTempFile() {
    }

    private fun createPayload(
        nonce: String,
        type: PayloadType,
        path: String,
        method: HttpMethod,
        mediaType: MediaType,
        data: String
    ): PayloadData {
        return PayloadData(
            nonce = nonce,
            type = type,
            tag = "test-tag", // TODO: pass a context and use that?
            token = "test-token",
            conversationId = "test-conversation-id",
            isEncrypted = false,
            path = path,
            method = method,
            mediaType = mediaType,
            data = data.toByteArray()
        )
    }
}
