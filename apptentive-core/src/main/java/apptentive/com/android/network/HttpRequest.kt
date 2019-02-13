package apptentive.com.android.network

import apptentive.com.android.concurrent.ExecutionQueue
import apptentive.com.android.network.Constants.DEFAULT_REQUEST_TIMEOUT

/**
 * Base class for HTTP requests.
 */
abstract class HttpRequest<T>(val method: HttpMethod, val url: String, val tag: String? = null) {
    /**
     * HTTP request headers.
     */
    val headers = MutableHttpHeaders()

    /**
     * Request timeout in seconds.
     */
    var timeout = DEFAULT_REQUEST_TIMEOUT

    /**
     * Execution queue for invoking callbacks (request would be
     * dispatched on a worker thread if missing)
     */
    var callbackQueue: ExecutionQueue? = null

    /**
     * Retry policy for the request.
     */
    var retryPolicy: HttpRequestRetryPolicy? = null

    /**
     * Holds the number of times this request has been retried.
     */
    internal var numRetries: Int = 0

    //region Inheritance

    /**
     * Returns a raw POST or PUT body to be sent.
     */
    protected abstract fun createRequestBody(): ByteArray?

    /**
     * Must be implemented to parse the raw network response.
     * This method will be called from a background thread.
     */
    protected abstract fun parseResponseObject(bytes: ByteArray): T

    //endregion

    //region Request/Response

    /**
     * Same as [createRequestBody] but with a limited visibility for the caller.
     */
    internal fun getRequestBody(): ByteArray? = createRequestBody()

    /**
     * Same as [parseResponseObject] but with a limited visibility for the caller.
     */
    internal fun readResponseObject(data: ByteArray): T = parseResponseObject(data)

    //endregion
}