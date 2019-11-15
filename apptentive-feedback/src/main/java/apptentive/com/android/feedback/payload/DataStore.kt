package apptentive.com.android.feedback.payload

interface DataStore {
    fun saveData(name: String, data: ByteArray)
    fun readData(nonce: String): ByteArray?
    fun deleteData(nonce: String): Boolean
}