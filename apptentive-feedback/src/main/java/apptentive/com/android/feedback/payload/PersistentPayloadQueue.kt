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
        val entity = dbHelper.nextUnsentPayload() ?: return null

        // FIXME: if you don't have payload data on the disk anymore - delete entity
        val data = dataStore.readData(entity.nonce) ?: return null

        // FIXME: if type is unknown - delete entity
        val type = PayloadType.parse(entity.type) ?: return null

        // FIXME: if media type is unknown - delete entity
        val mediaType = MediaType.parse(entity.mediaType)

        return Payload(
            nonce = entity.nonce,
            type = type,
            mediaType = mediaType,
            data = data
        )
    }

    override fun deletePayload(payload: Payload) {
        dataStore.deleteData(payload.nonce)
        dbHelper.deletePayload(payload.nonce)
    }
}