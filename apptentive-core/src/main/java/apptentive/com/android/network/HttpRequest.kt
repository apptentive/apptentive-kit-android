package apptentive.com.android.network

import apptentive.com.android.concurrent.ExecutionQueue
import apptentive.com.android.core.TimeInterval
import apptentive.com.android.core.UNDEFINED
import apptentive.com.android.network.Constants.DEFAULT_REQUEST_TIMEOUT
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.network
import kotlin.math.min
import kotlin.random.Random

/**
 * Successful request completion callback.
 */
typealias HttpRequestSuccessCallback<T> = (request: HttpRequest<T>, response: HttpResponse<T>) -> Unit

/**
 * Failed request completion callback.
 */
typealias HttpRequestErrorCallback<T> = (request: HttpRequest<T>, exception: Exception) -> Unit

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

    /**
     * Successful request completion callback.
     */
    private var successCallback: HttpRequestSuccessCallback<T>? = null

    /**
     * Failed request completion callback.
     */
    private var errorCallback: HttpRequestErrorCallback<T>? = null

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

    //region Listener notifications

    fun onSuccess(callback: HttpRequestSuccessCallback<T>): HttpRequest<T> {
        successCallback = callback
        return this
    }

    fun onError(callback: HttpRequestErrorCallback<T>): HttpRequest<T> {
        errorCallback = callback
        return this
    }

    internal fun notifySuccess(response: HttpResponse<T>) {
        val callbackQueue = this.callbackQueue
        if (callbackQueue != null) {
            callbackQueue.dispatch {
                notifySuccessGuarded(response)
            }
        } else {
            notifySuccessGuarded(response)
        }
    }

    internal fun notifyError(e: Exception) {
        val callbackQueue = this.callbackQueue
        if (callbackQueue != null) {
            callbackQueue.dispatch {
                notifyErrorGuarded(e)
            }
        } else {
            notifyErrorGuarded(e)
        }
    }

    private fun notifySuccessGuarded(response: HttpResponse<T>) {
        try {
            successCallback?.invoke(this, response)
        } catch (e: Exception) {
            notifyErrorGuarded(e)
        }
    }

    private fun notifyErrorGuarded(e: Exception) {
        try {
            errorCallback?.invoke(this, e)
        } catch (e: Exception) {
            Log.e(network, "Exception while notifying request listener", e)
        }
    }

    //endregion
}

/**
 * Retry policy for HTTP-request.
 */
interface HttpRequestRetryPolicy {
    /**
     * Determines if request should be retried.
     * @param statusCode HTTP-status code of the request.
     * @param numRetries number of times the request was already retried.
     */
    fun shouldRetry(statusCode: Int, numRetries: Int): Boolean

    /**
     * Returns a delay for the next retry.
     * @param numRetries number of times the request was already retried.
     */
    fun getRetryDelay(numRetries: Int): TimeInterval
}

/**
 * Default retry policy for HTTP-request (will be used unless overwritten).
 */
class HttpRequestRetryPolicyDefault(
    private val maxNumRetries: Int = Constants.DEFAULT_RETRY_MAX_COUNT,
    private val retryDelay: TimeInterval = Constants.DEFAULT_RETRY_DELAY
) : HttpRequestRetryPolicy {
    override fun shouldRetry(statusCode: Int, numRetries: Int): Boolean {
        if (statusCode in 400..499) {
            return false // don't retry if request was unauthorized or rejected
        }

        if (maxNumRetries == UNDEFINED) {
            return true // retry indefinitely
        }

        return numRetries <= maxNumRetries
    }

    override fun getRetryDelay(numRetries: Int): TimeInterval {
        // exponential back-off
        val temp = min(MAX_RETRY_CAP, retryDelay * Math.pow(2.0, (numRetries - 1).toDouble()))
        return temp / 2 * (1.0 + Random.nextDouble())
    }

    companion object {
        /**
         * Maximum retry timeout for the exponential back-off
         */
        private const val MAX_RETRY_CAP: TimeInterval = 10 * 60.0
    }
}