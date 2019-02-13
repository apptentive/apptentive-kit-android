package apptentive.com.android.network

import java.io.IOException

/**
 * Mock HTTP-request implementation for unit testing.
 * @param [tag] optional tag of the request
 * @param [statusCode] HTTP-response status code (defaults to 200)
 * @param [content] optional response content
 * @param [responseHeaders] optional response headers
 * @param [exceptionOnSend] indicates if exception should be thrown when sending request
 * @param [exceptionOnReceive] indicates if exception should be thrown when receiving request
 * @param [method] HTTP-request method (defaults to GET)
 */
class MockHttpRequest(
    tag: String? = null,
    private val statusCode: Int = 200,
    private val content: ByteArray? = null,
    private val responseHeaders: HttpHeaders? = null,
    private val exceptionOnSend: Boolean = false,
    private val exceptionOnReceive: Boolean = false,
    method: HttpMethod = HttpMethod.GET
) : HttpRequest<String>(method, "https://example.com", tag) {

    override fun createRequestBody(): ByteArray? {
        if (exceptionOnSend) {
            throw IOException("Exception while sending")
        }

        return mockResponse.content
    }

    override fun parseResponseObject(bytes: ByteArray): String {
        if (exceptionOnReceive) {
            throw IOException("Exception while receiving")
        }

        return String(bytes, Charsets.UTF_8)
    }

    val mockResponse
        get() = HttpNetworkResponse(
            statusCode = statusCode,
            statusMessage = getStatusMessage(statusCode),
            content = content ?: ByteArray(0),
            headers = responseHeaders ?: MutableHttpHeaders(),
            duration = 0.0
        )

    private fun getStatusMessage(statusCode: Int): String {
        return when (statusCode) {
            200 -> "OK"
            400 -> "Bad Request"
            500 -> "Internal Server Error"
            else -> "Unknown"
        }
    }

    override fun toString(): String {
        var result = ""
        if (tag != null) {
            result += "$tag "
        }
        result += "$statusCode ${getStatusMessage(statusCode)}"
        return result
    }
}