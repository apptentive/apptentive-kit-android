package apptentive.com.android.network

import apptentive.com.android.concurrent.ExecutionQueue
import apptentive.com.android.convert.Deserializer
import apptentive.com.android.convert.Serializer
import apptentive.com.android.network.Constants.DEFAULT_REQUEST_TIMEOUT

/**
 * Base class for HTTP requests.
 */
class HttpRequest<T>(
    val method: HttpMethod,
    val url: String,
    private val responseDeserializer: Deserializer<T>,
    private val requestSerializer: Serializer? = null,
    val tag: String? = null
) {
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
     * An optional user data associated with this request
     */
    var userData: Any? = null

    //region Initialization

    init {
        if (requestSerializer != null && method != HttpMethod.POST && method != HttpMethod.PUT) {
            throw IllegalArgumentException("Request body cannot be used with $method method: only POST or PUT are allowed")
        }
    }

    //endregion

    //region Request/Response

    /**
     * Returns a raw POST or PUT body to be sent.
     * This method will be called from a background thread.
     */
    internal fun getRequestBody(): ByteArray? {
        return requestSerializer?.serialize()
    }

    /**
     * Creates an instance of the response content from an array of bytes.
     * This method will be called from a background thread.
     */
    internal fun readResponseObject(data: ByteArray): T {
        return responseDeserializer.deserialize(data)
    }

    //endregion
}