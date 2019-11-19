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
        val actual1 = PayloadMetadata(
            nonce = "nonce-1",
            type = PayloadType.Event.toString(),
            mediaType = MediaType.applicationJson.toString()
        )
        val actual2 = PayloadMetadata(
            nonce = "nonce-2",
            type = PayloadType.AppRelease.toString(),
            mediaType = MediaType.applicationJson.toString()
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
}