package apptentive.com.android.feedback.payload

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import apptentive.com.android.data.DataStore
import apptentive.com.android.debug.Assert
import org.junit.After
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class PersistentPayloadQueueTest {
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
            data = "Payload - 1".toByteArray()
        )

        val queue = PersistentPayloadQueue(
            dbHelper = dbHelper,
            dataStore = MemoryDataStore()
        )

        queue.enqueuePayload(actual1)
        queue.enqueuePayload(actual2)

        Assert.assertEqual(actual1, queue.nextUnsentPayload())
        Assert.assertEqual(actual1, queue.nextUnsentPayload())

        queue.deletePayload(actual1)

        Assert.assertEqual(actual2, queue.nextUnsentPayload())
        Assert.assertEqual(actual2, queue.nextUnsentPayload())

        queue.deletePayload(actual2)

        assertNull(queue.nextUnsentPayload())
    }
}

private class MemoryDataStore : DataStore {
    private val store = mutableMapOf<String, ByteArray>()

    override fun saveData(key: String, data: ByteArray) {
        store[key] = data
    }

    override fun readData(key: String) = store[key]

    override fun deleteData(key: String) = store.remove(key) != null
}
