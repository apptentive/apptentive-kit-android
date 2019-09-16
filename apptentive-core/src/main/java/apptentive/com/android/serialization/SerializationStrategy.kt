package apptentive.com.android.serialization

interface SerializationStrategy<in T> {
    fun serialize(output: Encoder, obj: T)
}