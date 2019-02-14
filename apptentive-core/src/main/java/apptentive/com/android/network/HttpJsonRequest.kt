package apptentive.com.android.network

import apptentive.com.android.convert.JsonDeserializer
import apptentive.com.android.convert.JsonSerializer

/**
 * Helper function for creating JSON HTTP-requests.
 * @param content optional object which would be converted to JSON POST-body.
 */
inline fun <reified T> HttpJsonRequest(
    method: HttpMethod,
    url: String,
    content: Any? = null,
    tag: String? = null
): HttpRequest<T> {
    return HttpRequest(
        method = method,
        requestSerializer = if (content != null) JsonSerializer(content) else null,
        responseDeserializer = JsonDeserializer(T::class.java),
        url = url,
        tag = tag
    )
}

