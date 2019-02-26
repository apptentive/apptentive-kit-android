package apptentive.com.android.convert

import java.io.Closeable
import java.io.OutputStream

/**
 * Class responsible for serializing objects to an output stream.
 * @param stream target stream for the serialization.
 * @param autoClose flag indicating if serializer should close the stream when done.
 */
abstract class StreamSerializer(
    private val stream: OutputStream,
    private val autoClose: Boolean
) : Serializer,
    Closeable {
    override fun serialize(obj: Any) {
        try {
            serialize(stream, obj)
        } finally {
            if (autoClose) {
                close()
            }
        }
    }

    abstract fun serialize(stream: OutputStream, obj: Any)

    override fun close() {
        stream.close()
    }
}