package apptentive.com.android.convert

class JsonSerializer(private val obj: Any) : Serializer {
    override fun serialize(): ByteArray {
        return JsonConverter.toJson(obj).toByteArray(Charsets.UTF_8)
    }
}