package apptentive.com.android.core.serialization

internal interface TypeEncoder<in T> {
    fun encode(encoder: Encoder, value: T)
}
