package apptentive.com.android.network

import apptentive.com.android.concurrent.ExecutionQueue
import apptentive.com.android.convert.Deserializer
import apptentive.com.android.convert.Serializer
import apptentive.com.android.core.TimeInterval
import apptentive.com.android.network.Constants.DEFAULT_REQUEST_TIMEOUT

/**
 * Base class for HTTP-requests.
 * @param method HTTP-request method.
 * @param url URL
 * @param responseDeserializer deserializer responsible for converting raw response to a typed response object.
 * @param requestSerializer optional serializer for converting request object to raw POST body.
 * @param timeout request timeout.
 * @param tag optional tag for request identification.
 * @param callbackQueue optional execution queue for invoking callbacks.
 * @param retryPolicy optional retry policy for the request.
 * @param userData optional user data associated with the request.
 */
class HttpRequest<T>(
    val method: HttpMethod,
    val url: String,
    private val responseDeserializer: Deserializer<T>,
    private val requestSerializer: Serializer? = null,
    val timeout: TimeInterval = DEFAULT_REQUEST_TIMEOUT,
    val tag: String? = null,
    val callbackQueue: ExecutionQueue? = null,
    val retryPolicy: HttpRequestRetryPolicy? = null,
    val userData: Any? = null
) {
    /**
     * HTTP request headers.
     */
    val headers = MutableHttpHeaders()

    /**
     * Number of retries for this request.
     */
    internal var numRetries: Int = 0

    //region Initialization

    init {
        // only POST and PUT requests are allowed to have bodies
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
    internal fun createRequestBody(): ByteArray? {
        return requestSerializer?.serialize()
    }

    /**
     * Creates an instance of the response content from an array of bytes.
     * This method will be called from a background thread.
     */
    internal fun createResponseObject(data: ByteArray): T {
        return responseDeserializer.deserialize(data)
    }

    //endregion
}