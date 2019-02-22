package apptentive.com.android.convert.json

import apptentive.com.android.convert.Serializer
import java.io.OutputStream

class JsonSerializer : Serializer {
    override fun write(stream: OutputStream, target: Any) {
        val json = JsonConverter.toJson(target)
        val bytes = json.toByteArray(Charsets.UTF_8)
        stream.write(bytes)
    }
}