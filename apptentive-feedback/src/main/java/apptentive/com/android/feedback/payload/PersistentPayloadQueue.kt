package apptentive.com.android.feedback.payload

import apptentive.com.android.data.DataStore

class PersistentPayloadQueue(private val dataStore: DataStore) : PayloadQueue {
    /*
    nonce: Text
    type: Text,
    mediaType: Text,  application/json
     */
    override fun enqueuePayload(payload: Payload) {
        dataStore.saveData(payload.nonce, payload.data)
        // 1. store payload data in a file and get the filename (use uuid for filename)
        // 2. convert Payload to PayloadEntity (if using room)
        // 3. insert payload entity into database
    }

    override fun nextUnsentPayload(): Payload? {
        val payload: Payload = null ?: return null
        val data = dataStore.readData(payload.nonce)
        // 1. resolve filename for payload data
        // 2. read payload data from a file
        // 3. create Payload instance
        TODO()
    }

    override fun deletePayload(payload: Payload) {
        dataStore.deleteData(payload.nonce)
        // 1. resolve filename for payload data
        // 2. delete payload file
        // 3. delete entity
    }
}