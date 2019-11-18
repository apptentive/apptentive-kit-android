package apptentive.com.android.data

interface DataStore {
    fun saveData(key: String, data: ByteArray)
    fun readData(key: String): ByteArray?
    fun deleteData(key: String): Boolean
}