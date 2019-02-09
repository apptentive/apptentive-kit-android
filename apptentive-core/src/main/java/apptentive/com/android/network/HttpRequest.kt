package apptentive.com.android.network

import apptentive.com.android.concurrent.ExecutionQueue
import apptentive.com.android.core.TimeInterval
import apptentive.com.android.core.UNDEFINED
import apptentive.com.android.network.Constants.DEFAULT_REQUEST_TIMEOUT
import kotlin.math.min
import kotlin.random.Random

/**
 * Base class for HTTP requests.
 */
abstract class HttpRequest(val method: HttpMethod, val url: String) {
    /**
     * HTTP status code for the request. Null-value means request was not sent or failed due to an exception.
     */
    var statusCode: Int? = null
        internal set

    /**
     * HTTP status message for the request.
     */
    var statusMessage: String? = null
        internal set

    /**
     * HTTP error message.
     */
    var errorMessage: String? = null
        internal set

    /**
     * HTTP request headers.
     */
    val requestHeaders = MutableHttpHeaders()

    /**
     * HTTP response headers. Null-value means request was not sent or failed due to an exception.
     */
    var responseHeaders: HttpHeaders? = null

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
     * Returns the raw POST or PUT body to be sent.
     */
    protected abstract fun createRequestBody(): ByteArray?

    /**
     * Must be implemented to parse the raw network response.
     * This method will be called from a background thread.
     */
    protected abstract fun parseResponseBody(bytes: ByteArray)

    //endregion

    //region Request/Response

    /**
     * Same as [createRequestBody] but with a limited visibility for the caller.
     */
    internal fun getRequestBody(): ByteArray? = createRequestBody()

    /**
     * Same as [parseResponseBody] but with a limited visibility for the caller.
     */
    internal fun handleResponseBody(response: ByteArray) = parseResponseBody(response)

    //endregion
}

/**
 * Retry policy for HTTP-request.
 */
interface HttpRequestRetryPolicy {
    /**
     * Determines if [request] should be retried.
     */
    fun shouldRetry(request: HttpRequest): Boolean

    /**
     * Returns a delay for the [request]'s next retry attempt.
     */
    fun getRetryDelay(request: HttpRequest): TimeInterval
}

/**
 * Default retry policy for HTTP-request (will be used unless overwritten).
 */
class HttpRequestRetryPolicyDefault(
    private val maxNumRetries: Int = Constants.DEFAULT_RETRY_MAX_COUNT,
    private val retryDelay: TimeInterval = Constants.DEFAULT_RETRY_DELAY
) : HttpRequestRetryPolicy {
    override fun shouldRetry(request: HttpRequest): Boolean {
        if (request.statusCode in 400..499) {
            return false // don't retry if request was unauthorized or rejected
        }

        if (maxNumRetries == UNDEFINED) {
            return true // retry indefinitely
        }

        return request.numRetries <= maxNumRetries
    }

    override fun getRetryDelay(request: HttpRequest): TimeInterval {
        // exponential back-off
        val temp = min(MAX_RETRY_CAP, retryDelay * Math.pow(2.0, (request.numRetries - 1).toDouble()))
        return temp / 2 * (1.0 + Random.nextDouble())
    }

    companion object {
        /**
         * Maximum retry timeout for the exponential back-off
         */
        private const val MAX_RETRY_CAP: TimeInterval = 10 * 60.0
    }
}