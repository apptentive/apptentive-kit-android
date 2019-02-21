package apptentive.com.android.network

import apptentive.com.android.concurrent.ExecutionQueue
import apptentive.com.android.convert.Deserializer
import apptentive.com.android.core.TimeInterval
import apptentive.com.android.network.Constants.DEFAULT_REQUEST_TIMEOUT
import java.io.OutputStream
import java.lang.IllegalStateException
import java.net.URL

/**
 * Base class for HTTP-requests.
 * @param method HTTP-request method.
 * @param url URL
 * @param responseDeserializer deserializer responsible for converting raw response to a typed response object.
 * @param requestBody optional request body.
 * @param headers HTTP-request headers
 * @param timeout request timeout.
 * @param tag optional tag for request identification.
 * @param callbackQueue optional execution queue for invoking callbacks.
 * @param retryPolicy optional retry policy for the request.
 * @param userData optional user data associated with the request.
 */
class HttpRequest<T> private constructor(
    val method: HttpMethod,
    val url: URL,
    private val responseDeserializer: Deserializer<T>,
    internal val requestBody: HttpRequestBody?,
    internal val headers: HttpHeaders,
    val timeout: TimeInterval,
    val tag: String?,
    internal val callbackQueue: ExecutionQueue?,
    internal val retryPolicy: HttpRequestRetryPolicy?,
    val userData: Any?
) {
    /**
     * Number of retries for this request.
     */
    internal var numRetries: Int = 0

    //region Request/Response

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
        private val headers = MutableHttpHeaders()
        private lateinit var requestUrl: URL
        private lateinit var responseDeserializer: Deserializer<T>
        private var method = HttpMethod.GET
        private var requestBody: HttpRequestBody? = null
        private var callbackQueue: ExecutionQueue? = null
        private var retryPolicy: HttpRequestRetryPolicy? = null
        private var tag: String? = null
        private var userData: Any? = null
        private var timeout = DEFAULT_REQUEST_TIMEOUT

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
                method != HttpMethod.DELETE
            ) {
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

        //region Response

        fun deserializeWith(deserializer: Deserializer<T>): Builder<T> {
            this.responseDeserializer = deserializer
            return this
        }

        //endregion

        //region Execution Queue

        fun dispatchOn(queue: ExecutionQueue): Builder<T> {
            callbackQueue = queue
            return this
        }

        //endregion

        //region Retry Policy

        fun retryWith(policy: HttpRequestRetryPolicy?): Builder<T> {
            this.retryPolicy = policy
            return this
        }

        //endregion

        //region Tag

        fun tag(tag: String?): Builder<T> {
            this.tag = tag
            return this
        }

        //endregion

        //region User Data

        fun userData(userData: Any?): Builder<T> {
            this.userData = userData
            return this
        }

        //endregion

        //region Build

        fun build(): HttpRequest<T> {
            if (!this::requestUrl.isInitialized) {
                throw IllegalStateException("Builder is missing a url")
            }
            if (!this::responseDeserializer.isInitialized) {
                throw IllegalStateException("Builder is missing a url")
            }

            return HttpRequest(
                method = method,
                url = requestUrl,
                responseDeserializer = responseDeserializer,
                requestBody = requestBody,
                headers = headers,
                timeout = timeout,
                tag = tag,
                callbackQueue = callbackQueue,
                retryPolicy = retryPolicy,
                userData = userData
            )
        }

        //endregion
    }
    //endregion
}