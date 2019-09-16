package apptentive.com.android.serialization

interface Serializer<T> : SerializationStrategy<T>, DeserializationStrategy<T>