package apptentive.com.android.core.serialization

internal interface TypeSerializer<T> : TypeEncoder<T>, TypeDecoder<T>
