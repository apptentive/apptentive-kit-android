package apptentive.com.android.convert.json

import apptentive.com.android.convert.StreamDeserializer
import java.io.File
import java.io.InputStream

/**
 * Class responsible for de-serializing JSON-object from an input stream.
 * @param stream target stream for the serialization.
 * @param type JSON-object type
 * @param autoClose flag indicating if serializer should close the stream when done.
 */
class JsonStreamDeserializer(
    stream: InputStream,
    private val type: Class<*>,
    autoClose: Boolean = true
) : StreamDeserializer(stream, autoClose) {
    override fun deserialize(stream: InputStream): Any {
        val json = readJsonString(stream)
        return JsonConverter.fromJson(json, type)
    }

    companion object {
        /**
         * Creates an instance of [JsonStreamDeserializer] with a file as a target for the de-serialization.
         * @param file target file.
         */
        inline fun <reified T> fromFile(file: File): JsonStreamDeserializer {
            if (!file.exists()) {
                throw IllegalArgumentException("File does not exist: $file")
            }
            val stream = file.inputStream()
            return JsonStreamDeserializer(stream, T::class.java, autoClose = true)
        }
    }
}

/**
 * Helper function for reading JSON-string from an input stream.
 */
private fun readJsonString(stream: InputStream): String {
    val bytes = stream.readBytes()
    if (bytes.isEmpty()) {
        return "{}"
    }
    return bytes.toString(Charsets.UTF_8)
}