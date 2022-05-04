package apptentive.com.android.serialization

import apptentive.com.android.util.InternalUseOnly

@InternalUseOnly
interface TypeDecoder<out T> {
    fun decode(decoder: Decoder): T
}
