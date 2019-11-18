package apptentive.com.android.feedback.payload

import apptentive.com.android.data.DataStore

class PersistentPayloadQueue(
    private val dbHelper: PayloadSQLiteHelper,
    private val dataStore: DataStore
) : PayloadQueue {
    override fun enqueuePayload(payload: Payload) {
        val entity = PayloadEntity.fromModel(payload)
        // 1. store payload data to a file
        dataStore.saveData(payload.nonce, payload.data)
        // 2. store entity into the database
        dbHelper.addPayload(entity)
    }

    override fun nextUnsentPayload(): Payload? {
        var entity = dbHelper.nextUnsentPayload()

        while (entity != null) {
            try {
                val data = readPayloadData(entity.nonce)
                val type = PayloadType.parse(entity.type)
                val mediaType = MediaType.parse(entity.mediaType)

                return Payload(
                    nonce = entity.nonce,
                    type = type,
                    mediaType = mediaType,
                    data = data
                )
            } catch (e: Exception) {
                // FIXME: log exception
                dbHelper.deletePayload(entity.nonce)
                entity = dbHelper.nextUnsentPayload()
            }
        }

        return null
    }

    override fun deletePayload(payload: Payload) {
        dataStore.deleteData(payload.nonce)
        dbHelper.deletePayload(payload.nonce)
    }

    private fun readPayloadData(nonce: String): ByteArray {
        return dataStore.readData(nonce) ?: throw MissingPayloadDataException(nonce)
    }
}