package apptentive.com.android.convert.json

import apptentive.com.android.convert.StreamSerializer
import java.io.File
import java.io.OutputStream

/**
 * Class responsible for serializing JSON-object to an output stream.
 * @param stream target stream for the serialization.
 * @param autoClose flag indicating if serializer should close the stream when done.
 */
class JsonStreamSerializer(
    stream: OutputStream,
    autoClose: Boolean = true
) : StreamSerializer(stream, autoClose) {
    override fun serialize(stream: OutputStream, obj: Any) {
        val json = JsonConverter.toJson(obj)
        val bytes = json.toByteArray(Charsets.UTF_8)
        stream.write(bytes)
    }

    companion object {
        /**
         * Creates an instance of [JsonStreamSerializer] with a file as a target for the serialization.
         * @param file target file.
         */
        fun fromFile(file: File): JsonStreamSerializer {
            if (file.exists() && !file.isFile) {
                throw IllegalArgumentException("Invalid file: $file")
            }

            val stream = file.outputStream()
            return JsonStreamSerializer(stream, autoClose = true)
        }
    }
}