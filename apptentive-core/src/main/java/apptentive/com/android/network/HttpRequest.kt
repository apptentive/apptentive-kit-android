package apptentive.com.android.network

import apptentive.com.android.concurrent.ExecutionQueue
import apptentive.com.android.convert.Deserializer
import apptentive.com.android.convert.Serializer
import apptentive.com.android.core.TimeInterval
import apptentive.com.android.network.Constants.DEFAULT_REQUEST_TIMEOUT
import java.lang.IllegalStateException
import java.net.URL

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

    //region Builder

    //FIXME: doc-comments
    class Builder<T> {
        private lateinit var requestUrl: URL
        private var method = HttpMethod.GET
        private var requestBody: HttpRequestBody? = null
        private val headers = MutableHttpHeaders()

        //region URL

        fun url(url: String) = url(URL(url))

        fun url(url: URL): Builder<T> {
            this.requestUrl = url
            return this
        }

        //endregion

        //region Method

        fun get() = method(HttpMethod.GET)
        fun head() = method(HttpMethod.HEAD)
        fun post(body: HttpRequestBody) = method(HttpMethod.POST, body)
        fun put(body: HttpRequestBody) = method(HttpMethod.PUT, body)
        fun delete(body: HttpRequestBody? = null) = method(HttpMethod.DELETE, body)
        fun patch(body: HttpRequestBody) = method(HttpMethod.PATCH, body)

        fun method(method: HttpMethod, requestBody: HttpRequestBody? = null): Builder<T> {
            if (requestBody != null &&
                method != HttpMethod.POST &&
                method != HttpMethod.PUT &&
                method != HttpMethod.PATCH &&
                method != HttpMethod.DELETE) {
                throw IllegalArgumentException("Request requestBody cannot be used with $method method")
            }

            this.method = method
            this.requestBody = requestBody
            return this
        }

        //endregion

        //region Headers

        fun header(name: String, value: String): Builder<T> {
            headers[name] = value
            return this
        }

        fun header(name: String, value: Int): Builder<T> {
            headers[name] = value
            return this
        }

        fun header(name: String, value: Boolean): Builder<T> {
            headers[name] = value
            return this
        }

        fun headers(headers: HttpHeaders): Builder<T> {
            this.headers.clear()
            this.headers.addAll(headers)
            return this
        }

        //endregion

        //region Build

        fun build(): HttpRequest<T> {
            if (!this::requestUrl.isInitialized) {
                throw IllegalStateException("Builder is missing a url")
            }

            TODO()
        }

        //endregion
    }
    //endregion
}