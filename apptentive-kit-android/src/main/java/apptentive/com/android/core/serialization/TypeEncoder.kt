package apptentive.com.android.core.serialization

import apptentive.com.android.core.util.InternalUseOnly

@InternalUseOnly
interface TypeEncoder<in T> {
    fun encode(encoder: Encoder, value: T)
}
