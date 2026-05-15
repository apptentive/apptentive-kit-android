package apptentive.com.android.core.serialization

internal interface TypeDecoder<out T> {
    fun decode(decoder: Decoder): T
}
