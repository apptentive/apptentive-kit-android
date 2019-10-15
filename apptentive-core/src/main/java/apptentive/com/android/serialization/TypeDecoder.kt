package apptentive.com.android.serialization

interface TypeDecoder<out T> {
    fun decode(decoder: Decoder): T
}