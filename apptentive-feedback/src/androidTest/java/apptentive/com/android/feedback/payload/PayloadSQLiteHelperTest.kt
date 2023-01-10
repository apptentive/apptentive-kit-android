package apptentive.com.android.feedback.payload

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import apptentive.com.android.TestCase
import apptentive.com.android.debug.Assert.assertEqual
import apptentive.com.android.encryption.EncryptionFactory
import apptentive.com.android.encryption.NotEncrypted
import apptentive.com.android.network.HttpMethod
import org.junit.After
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class PayloadSQLiteHelperTest : TestCase() {
    private lateinit var dbHelper: PayloadSQLiteHelper

    private val context: Context get() = ApplicationProvider.getApplicationContext<Context>()

    @Before
    fun setupDb() {
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
            path = path,
            method = method,
            mediaType = mediaType,
            data = data.toByteArray()
        )
    }
}
