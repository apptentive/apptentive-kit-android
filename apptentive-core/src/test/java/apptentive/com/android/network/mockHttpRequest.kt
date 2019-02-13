package apptentive.com.android.network

import apptentive.com.android.convert.Deserializer
import apptentive.com.android.convert.Serializer
import java.io.IOException

/**
 * Creates mock string HTTP-request.
 */
internal fun createMockHttpRequest(
    tag: String? = null,
    method: HttpMethod = HttpMethod.GET,
    url: String? = null,
    statusCode: Int = 200,
    response: String? = null,
    responseHeaders: HttpHeaders? = null,
    exceptionOnSend: Boolean = false,
    exceptionOnReceive: Boolean = false,
    retryPolicy: HttpRequestRetryPolicy? = null
): HttpRequest<String> {
    val content = response?.toByteArray() ?: ByteArray(0)

    val includesBody = method == HttpMethod.POST || method == HttpMethod.PUT
    val requestSerializer = if (includesBody) object : Serializer {
        override fun serialize(): ByteArray {
            if (exceptionOnSend) {
                throw IOException("failed to send")
            }

            return content
        }
    } else null

    val responseDeserializer = object : Deserializer<String> {
        override fun deserialize(bytes: ByteArray): String {
            if (exceptionOnReceive) {
                throw IOException("failed to receive")
            }

            return String(bytes)
        }
    }


    return createMockHttpRequest(
        responseDeserializer = responseDeserializer,
        tag = tag,
        method = method,
        url = url,
        content = content,
        statusCode = statusCode,
        requestSerializer = requestSerializer,
        responseHeaders = responseHeaders,
        retryPolicy = retryPolicy
    )
}

/**
 * Creates a generic mock HTTP-request.
 */
internal fun <T> createMockHttpRequest(
    responseDeserializer: Deserializer<T>,
    tag: String? = null,
    method: HttpMethod = HttpMethod.GET,
    url: String? = null,
    statusCode: Int = 200,
    content: ByteArray? = null,
    requestSerializer: Serializer? = null,
    responseHeaders: HttpHeaders? = null,
    retryPolicy: HttpRequestRetryPolicy? = null
): HttpRequest<T> {
    return HttpRequest(
        tag = tag,
        method = method,
        url = url ?: "https://example.com",
        requestSerializer = requestSerializer,
        responseDeserializer = responseDeserializer,
        retryPolicy = retryPolicy,
        userData = HttpNetworkResponse(
            statusCode = statusCode,
            statusMessage = getStatusMessage(statusCode),
            content = content ?: ByteArray(0),
            headers = responseHeaders ?: MutableHttpHeaders(),
            duration = 0.0
        )
    )
}

private fun getStatusMessage(statusCode: Int): String {
    return when (statusCode) {
        200 -> "OK"
        204 -> "No Content"
        400 -> "Bad Request"
        500 -> "Internal Server Error"
        else -> "Unknown"
    }
}