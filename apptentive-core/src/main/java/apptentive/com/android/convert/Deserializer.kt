package apptentive.com.android.convert

interface Deserializer<T> {
    fun deserialize(byte: ByteArray): T
}