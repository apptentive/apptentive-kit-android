package apptentive.com.android.convert

class JsonDeserializer<T>(private val type: Class<T>) : Deserializer<T> {
    override fun deserialize(bytes: ByteArray): T {
        val json = if (bytes.isEmpty()) "{}" else String(bytes, Charsets.UTF_8)
        
        @Suppress("UNCHECKED_CAST")
        return JsonConverter.fromJson(json, type) as T
    }
}