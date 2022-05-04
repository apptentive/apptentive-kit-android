package apptentive.com.android.serialization

import apptentive.com.android.util.InternalUseOnly

@InternalUseOnly
interface TypeEncoder<in T> {
    fun encode(encoder: Encoder, value: T)
}
