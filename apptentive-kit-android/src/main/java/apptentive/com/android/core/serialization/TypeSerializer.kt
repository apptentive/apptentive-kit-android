package apptentive.com.android.core.serialization

import apptentive.com.android.core.util.InternalUseOnly

@InternalUseOnly
interface TypeSerializer<T> : TypeEncoder<T>, TypeDecoder<T>
