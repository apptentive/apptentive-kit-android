package apptentive.com.android.serialization

interface SerializationStrategy<in T> {
    fun serialize(obj: T)
}