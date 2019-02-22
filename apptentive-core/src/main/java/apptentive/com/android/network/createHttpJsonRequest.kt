package apptentive.com.android.network

import apptentive.com.android.convert.JsonConverter
import java.io.InputStream
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
    val requestBody = if (requestObject != null) HttpJsonRequestBody(requestObject) else null
    val responseReader = createHttpJsonResponseReader<T>()
    return HttpRequest.Builder<T>()
        .url(url)
        .method(method, requestBody)
        .responseReader(responseReader)
        .tag(tag)
        .userData(userData)
        .build()
}

class HttpJsonRequestBody(private val obj: Any) : HttpRequestBody {
    override val contentType: String
        get() = "application/json"

    override fun write(stream: OutputStream) {
        val json = JsonConverter.toJson(obj)
        stream.write(json.toByteArray(Charsets.UTF_8))
    }
}

inline fun <reified T> createHttpJsonResponseReader(): HttpResponseReader<T> {
    return HttpJsonResponseReader(T::class.java)
}

class HttpJsonResponseReader<T>(private val type: Class<T>) : HttpResponseReader<T> {
    override fun read(stream: InputStream): T {
        val bytes = stream.readBytes()
        val json = if (bytes.isEmpty()) "{}" else String(bytes, Charsets.UTF_8)

        @Suppress("UNCHECKED_CAST")
        return JsonConverter.fromJson(json, type) as T
    }
}

