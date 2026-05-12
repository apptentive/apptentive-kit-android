package apptentive.com.android.core.serialization

import apptentive.com.android.core.util.InternalUseOnly

@InternalUseOnly
interface TypeDecoder<out T> {
    fun decode(decoder: Decoder): T
}
