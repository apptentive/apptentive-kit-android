package apptentive.com.android.convert

interface Deserializer<T> {
    fun deserialize(bytes: ByteArray): T
}