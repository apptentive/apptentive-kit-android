package apptentive.com.android.network

import apptentive.com.android.convert.JsonDeserializer
import apptentive.com.android.convert.JsonSerializer

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
    return HttpRequest(
        method = method,
        requestSerializer = if (requestObject != null) JsonSerializer(requestObject) else null,
        responseDeserializer = JsonDeserializer(T::class.java),
        url = url,
        tag = tag,
        userData = userData
    )
}

