package apptentive.com.android.network

import apptentive.com.android.convert.json.JsonConverter
import apptentive.com.android.core.TimeInterval
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

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
    val overrideMethod = if (exceptionOnSend) HttpMethod.POST else method
    val content = response?.toByteArray() ?: ByteArray(0)
    val includesBody = overrideMethod == HttpMethod.POST || overrideMethod == HttpMethod.PUT

    val requestBody = if (includesBody) object : HttpRequestBody {
        override val contentType: String
            get() = "text/plain"

        override fun write(stream: OutputStream) {
            if (exceptionOnSend) {
                throw IOException("failed to send")
            }

            return stream.write(content)
        }
    } else null

    val responseReader = object : HttpResponseReader<String> {
        override fun read(stream: InputStream): String {
            if (exceptionOnReceive) {
                throw IOException("failed to receive")
            }

            return String(stream.readBytes())
        }
    }

    return createMockHttpRequest(
        responseReader = responseReader,
        tag = tag,
        method = overrideMethod,
        url = url,
        content = content,
        statusCode = statusCode,
        requestBody = requestBody,
        responseHeaders = responseHeaders,
        retryPolicy = retryPolicy
    )
}

internal fun createMockHttpRequest(
    responses: Array<HttpNetworkResponse>,
    tag: String? = null,
    method: HttpMethod = HttpMethod.GET,
    url: String? = null,
    requestBody: HttpRequestBody? = null,
    retryPolicy: HttpRequestRetryPolicy? = null
): HttpRequest<String> {
    val responseReader = object : HttpResponseReader<String> {
        override fun read(stream: InputStream): String {
            return String(stream.readBytes())
        }
    }

    return createMockHttpRequest(
        responses = responses,
        responseReader = responseReader,
        tag = tag,
        method = method,
        url = url,
        requestBody = requestBody,
        retryPolicy = retryPolicy
    )
}

/**
 * Creates a generic mock HTTP-request.
 */
internal fun <T> createMockHttpRequest(
    responseReader: HttpResponseReader<T>,
    tag: String? = null,
    method: HttpMethod = HttpMethod.GET,
    url: String? = null,
    statusCode: Int = 200,
    content: ByteArray? = null,
    requestBody: HttpRequestBody? = null,
    responseHeaders: HttpHeaders? = null,
    retryPolicy: HttpRequestRetryPolicy? = null
): HttpRequest<T> {
    val response = createNetworkResponse(
        statusCode = statusCode,
        content = content,
        responseHeaders = responseHeaders
    )
    return createMockHttpRequest(
        responses = arrayOf(response),
        responseReader = responseReader,
        requestBody = requestBody,
        url = url,
        method = method,
        retryPolicy = retryPolicy,
        tag = tag
    )
}

/**
 * Creates a generic mock HTTP-request with a sequence of responses.
 */
internal fun <T> createMockHttpRequest(
    responses: Array<HttpNetworkResponse>,
    responseReader: HttpResponseReader<T>,
    tag: String? = null,
    method: HttpMethod = HttpMethod.GET,
    url: String? = null,
    requestBody: HttpRequestBody? = null,
    retryPolicy: HttpRequestRetryPolicy? = null
): HttpRequest<T> {
    return HttpRequest.Builder<T>()
        .method(method, requestBody)
        .url(url ?: "https://example.com")
        .responseReader(responseReader)
        .tag(tag)
        .retryWith(retryPolicy)
        .userData(HttpNetworkResponseQueue(responses))
        .build()
}

internal inline fun <reified T : Any> createMockJsonRequest(
    responseObject: T,
    requestObject: Any? = null,
    tag: String? = null,
    method: HttpMethod = HttpMethod.GET,
    url: String? = null
): HttpRequest<T> {
    val userData = createNetworkResponses(
        content = JsonConverter.toJson(responseObject).toByteArray(Charsets.UTF_8)
    )
    val overrideMethod = if (requestObject != null) HttpMethod.POST else method
    return createHttpJsonRequest(
        method = overrideMethod,
        url = url ?: "https://example.com",
        requestObject = requestObject,
        tag = tag,
        userData = userData
    )
}


internal fun createNetworkResponses(vararg responses: HttpNetworkResponse): HttpNetworkResponseQueue {
    return HttpNetworkResponseQueue(responses)
}

/**
 * Helper function for creating mock network responses.
 */
internal fun createNetworkResponses(
    statusCode: Int = 200,
    content: ByteArray? = null,
    responseHeaders: HttpHeaders? = null,
    duration: TimeInterval = 0.0
): HttpNetworkResponseQueue {
    return createNetworkResponses(
        createNetworkResponse(
            statusCode = statusCode,
            content = content,
            responseHeaders = responseHeaders,
            duration = duration
        )
    )
}

/**
 * Helper function for creating mock network responses.
 */
internal fun createNetworkResponse(
    statusCode: Int = 200,
    content: ByteArray? = null,
    responseHeaders: HttpHeaders? = null,
    duration: TimeInterval = 0.0
): HttpNetworkResponse {
    return HttpNetworkResponse(
        statusCode = statusCode,
        statusMessage = getStatusMessage(statusCode),
        stream = ByteArrayInputStream(content ?: ByteArray(0)),
        headers = responseHeaders ?: MutableHttpHeaders(),
        duration = duration
    )
}

private fun getStatusMessage(statusCode: Int): String {
    return when (statusCode) {
        200 -> "OK"
        204 -> "No Content"
        401 -> "Unauthorized"
        500 -> "Internal Server Error"
        else -> "Unknown"
    }
}