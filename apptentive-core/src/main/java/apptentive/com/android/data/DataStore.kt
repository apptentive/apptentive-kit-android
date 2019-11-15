package apptentive.com.android.data

interface DataStore {
    fun saveData(name: String, data: ByteArray)
    fun readData(nonce: String): ByteArray?
    fun deleteData(nonce: String): Boolean
}