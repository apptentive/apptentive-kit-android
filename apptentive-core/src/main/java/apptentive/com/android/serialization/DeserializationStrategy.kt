package apptentive.com.android.serialization

interface DeserializationStrategy<T> {
    fun deserialize(): T
}