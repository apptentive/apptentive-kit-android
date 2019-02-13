package apptentive.com.android.convert

class JsonDeserializer<T>(private val type: Class<T>) : Deserializer<T> {
    override fun deserialize(byte: ByteArray): T {
        val json = String(byte, Charsets.UTF_8)

        @Suppress("UNCHECKED_CAST")
        return JsonConverter.fromJson(json, type) as T
    }

    companion object {
        inline fun <reified T> of(): Deserializer<T> {
            return JsonDeserializer(T::class.java)
        }
    }
}