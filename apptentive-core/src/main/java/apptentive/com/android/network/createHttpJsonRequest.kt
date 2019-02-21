package apptentive.com.android.network

import apptentive.com.android.convert.JsonConverter
import apptentive.com.android.convert.JsonDeserializer
import java.io.OutputStream

/**
 * Helper function for creating JSON HTTP-requests.
 * @param requestObject optional object which would be converted to JSON POST-body.
 */
inline fun <reified T> createHttpJsonRequest(
    method: HttpMethod,
    url: String,
    requestObject: Any? = null,
    tag: String? = null,
    userData: Any? = null
): HttpRequest<T> {
    return HttpRequest.Builder<T>()
        .url(url)
        .method(method, if (requestObject != null) HttpJsonRequestBody(requestObject) else null)
        .deserializeWith(JsonDeserializer(T::class.java))
        .tag(tag)
        .userData(userData)
        .build()
}

class HttpJsonRequestBody(private val obj: Any) : HttpRequestBody {
    override fun write(stream: OutputStream) {
        val json = JsonConverter.toJson(obj)
        stream.write(json.toByteArray(Charsets.UTF_8))
    }
}

