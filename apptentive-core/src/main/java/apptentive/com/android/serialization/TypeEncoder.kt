package apptentive.com.android.serialization

interface TypeEncoder<in T> {
    fun encode(encoder: Encoder, value: T)
}