package apptentive.com.android.feedback.payload

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import apptentive.com.android.debug.Assert.assertEqual
import junit.framework.Assert.assertNull
import org.junit.After
import org.junit.Before
import org.junit.Test

class PayloadSQLiteHelperTest {
    private lateinit var dbHelper: PayloadSQLiteHelper

    private val context: Context get() = ApplicationProvider.getApplicationContext<Context>()

    @Before
    fun setupDb() {
        dbHelper = PayloadSQLiteHelper(context)
    }

    @After
    fun closeDb() {
        dbHelper.close()
    }

    @Test
    fun addingAndRemovingPayloads() {
        val actual1 = Payload(
            nonce = "nonce-1",
            type = PayloadType.Event,
            mediaType = MediaType.applicationJson,
            data = "Payload - 1".toByteArray()
        )
        val actual2 = Payload(
            nonce = "nonce-2",
            type = PayloadType.AppRelease,
            mediaType = MediaType.applicationJson,
            data = "Payload - 2".toByteArray()
        )
        dbHelper.addPayload(actual1)
        dbHelper.addPayload(actual2)

        assertEqual(actual1, dbHelper.getNextUnsentPayload())
        assertEqual(actual1, dbHelper.getNextUnsentPayload())

        dbHelper.deletePayload(actual1)

        assertEqual(actual2, dbHelper.getNextUnsentPayload())
        assertEqual(actual2, dbHelper.getNextUnsentPayload())

        dbHelper.deletePayload(actual2)

        assertNull(dbHelper.getNextUnsentPayload())
    }
}