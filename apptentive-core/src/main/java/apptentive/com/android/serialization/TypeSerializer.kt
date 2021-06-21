package apptentive.com.android.serialization

interface TypeSerializer<T> : TypeEncoder<T>, TypeDecoder<T>
