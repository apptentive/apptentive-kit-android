package apptentive.com.android.network

import apptentive.com.android.concurrent.ExecutionQueue
import apptentive.com.android.network.Constants.DEFAULT_REQUEST_TIMEOUT

/**
 * Base class for HTTP requests.
 */
abstract class HttpRequest(val method: HttpMethod, val url: String) {
    /**
     * Headers to send with request.
     */
    val requestHeaders: MutableHttpHeaders = MutableHttpHeaders()

    /**
     * Request timeout in seconds.
     */
    val timeout = DEFAULT_REQUEST_TIMEOUT

    /**s
     * Execution queue for invoking callbacks (request would be
     * dispatched on a worker thread if missing)
     */
    var callbackQueue: ExecutionQueue? = null

    //region Inheritance

    /**
     * Returns the raw POST or PUT body to be sent.
     */
    protected abstract fun createRequestBody(): ByteArray?

    /**
     * Must be implemented to parse the raw network response.
     * This method will be called from a background thread.
     */
    protected abstract fun parseResponseBody(bytes: ByteArray)

    /**
     * Must be implemented to parse the raw error response.
     * This method will be called from a background thread.
     */
    protected abstract fun parseErrorBody(bytes: ByteArray)

    //endregion

    //region Request/Response

    internal fun getRequestBody(): ByteArray? = createRequestBody()
    internal fun handleResponseBody(response: ByteArray) = parseResponseBody(response)
    internal fun handleErrorBody(response: ByteArray) = parseErrorBody(response)

    //endregion
}