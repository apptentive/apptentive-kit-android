package apptentive.com.android.serialization

import apptentive.com.android.util.InternalUseOnly

@InternalUseOnly
interface TypeSerializer<T> : TypeEncoder<T>, TypeDecoder<T>
