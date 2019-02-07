package apptentive.com.android.network

import java.net.HttpURLConnection

abstract class HttpRequest(val url: String, val method: HttpMethod = HttpMethod.GET) {
    var responseCode = -1
        internal set
    var responseMessage: String = ""
        internal set

    var errorMessage: String? = null
        internal set

    val requestHeaders: MutableHttpHeaders = MutableHttpHeaders()
    val responseHeaders: MutableHttpHeaders = MutableHttpHeaders()

    /**
     * Returns true if request returned with a valid status code.
     */
    val isSuccessful: Boolean get() = responseCode >= HttpURLConnection.HTTP_OK && responseCode < HttpURLConnection.HTTP_MULT_CHOICE

    /**
     * Returns true if request failed due to the connection error or there was an exception while processing request.
     */
    val isFailed: Boolean get() = !isSuccessful || !errorMessage.isNullOrEmpty()

    //region Inheritance

    protected abstract fun serializeRequest(): ByteArray?
    protected abstract fun deserializeResponse(bytes: ByteArray)

    //endregion

    //region Request/Response

    internal fun createRequestBody(): ByteArray? = serializeRequest()
    internal fun processResponseBody(response: ByteArray) = deserializeResponse(response)

    //endregion
}