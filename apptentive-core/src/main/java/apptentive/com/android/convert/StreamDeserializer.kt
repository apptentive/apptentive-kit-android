package apptentive.com.android.convert

import java.io.Closeable
import java.io.InputStream

/**
 * Class responsible for de-serializing an object from an input stream.
 * @param stream target stream for the serialization.
 * @param autoClose flag indicating if serializer should close the stream when done.
 */
abstract class StreamDeserializer(
    private val stream: InputStream,
    private val autoClose: Boolean = true
) : Deserializer,
    Closeable {
    override fun deserialize(): Any {
        try {
            return deserialize(stream)
        } finally {
            if (autoClose) {
                close()
            }
        }
    }

    abstract fun deserialize(stream: InputStream): Any

    override fun close() {
        stream.close()
    }
}