package apptentive.com.android.convert

import java.io.InputStream

inline fun <reified T : Any> createJsonDeserializer(): JsonDeserializer<T> = JsonDeserializer(T::class.java)

class JsonDeserializer<T : Any>(private val type: Class<T>) : Deserializer {
    override fun read(stream: InputStream): T {
        val bytes = stream.readBytes()

        // we should treat empty bytes as an empty json
        val json = if (bytes.isEmpty()) "{}" else String(bytes, Charsets.UTF_8)

        @Suppress("UNCHECKED_CAST")
        return JsonConverter.fromJson(json, type) as T
    }
}