package apptentive.com.android.network

import apptentive.com.android.concurrent.ExecutionQueue
import apptentive.com.android.core.TimeInterval
import apptentive.com.android.network.Constants.DEFAULT_REQUEST_TIMEOUT
import java.io.InputStream
import java.lang.IllegalStateException
import java.net.URL

/**
 * Base class for HTTP-requests.
 *
 * @param method HTTP-request method.
 * @param url URL
 * @param responseReader deserializer responsible for converting raw response stream to a typed response object.
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
    private val responseReader: HttpResponseReader<T>,
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
     * Creates an instance of the response content from an input stream of bytes.
     * This method will be called from a background thread.
     */
    internal fun readResponseObject(stream: InputStream): T {
        return responseReader.read(stream)
    }

    //endregion

    //region Builder

    /**
     * Builder class for creating [HttpRequest] instances.
     */
    class Builder<T> {
        private val headers = MutableHttpHeaders()
        private lateinit var requestUrl: URL
        private lateinit var reader: HttpResponseReader<T>
        private var method = HttpMethod.GET
        private var requestBody: HttpRequestBody? = null
        private var callbackQueue: ExecutionQueue? = null
        private var retryPolicy: HttpRequestRetryPolicy? = null
        private var tag: String? = null
        private var userData: Any? = null
        private var timeout = DEFAULT_REQUEST_TIMEOUT

        //region URL

        /** Sets request URL */
        fun url(url: String) = url(URL(url))

        /** Sets request URL */
        fun url(url: URL): Builder<T> {
            this.requestUrl = url
            return this
        }

        //endregion

        //region Method

        /** Sets GET method */
        fun get() = method(HttpMethod.GET)

        /** Sets POST method with optional request body */
        fun post(body: HttpRequestBody? = null) = method(HttpMethod.POST, body)

        /** Sets PUT method with optional request body */
        fun put(body: HttpRequestBody? = null) = method(HttpMethod.PUT, body)

        /** Sets DELETE method with optional request body */
        fun delete(body: HttpRequestBody? = null) = method(HttpMethod.DELETE, body)

        /** Sets PATCH method with optional request body */
        fun patch(body: HttpRequestBody? = null) = method(HttpMethod.PATCH, body)

        /** Generic method for setting HTTP-method with optional request body */
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

        /** Sets request header */
        fun header(name: String, value: String): Builder<T> {
            headers[name] = value
            return this
        }

        /** Sets request header */
        fun header(name: String, value: Int): Builder<T> {
            headers[name] = value
            return this
        }

        /** Sets request header */
        fun header(name: String, value: Boolean): Builder<T> {
            headers[name] = value
            return this
        }

        /** Overwrite request headers */
        fun headers(headers: HttpHeaders): Builder<T> {
            this.headers.clear()
            this.headers.addAll(headers)
            return this
        }

        //endregion

        //region Response

        /** Sets HTTP-response reader */
        fun responseReader(responseReader: HttpResponseReader<T>): Builder<T> {
            this.reader = responseReader
            return this
        }

        //endregion

        //region Execution Queue

        /** Sets dispatch queue for callbacks */
        // FIXME: unit tests
        fun callbackOn(queue: ExecutionQueue): Builder<T> {
            callbackQueue = queue
            return this
        }

        //endregion

        //region Retry Policy

        /** Sets an optional retry policy */
        fun retryWith(policy: HttpRequestRetryPolicy?): Builder<T> {
            this.retryPolicy = policy
            return this
        }

        //endregion

        //region Tag

        /** Sets an optional request tag */
        fun tag(tag: String?): Builder<T> {
            this.tag = tag
            return this
        }

        //endregion

        //region User Data

        /** Sets an optional request user data */
        fun userData(userData: Any?): Builder<T> {
            this.userData = userData
            return this
        }

        //endregion

        //region Build

        /** Creates an HTTP-request instance */
        fun build(): HttpRequest<T> {
            if (!this::requestUrl.isInitialized) {
                throw IllegalStateException("Builder is missing a url")
            }
            if (!this::reader.isInitialized) {
                throw IllegalStateException("Builder is missing a response reader")
            }

            return HttpRequest(
                method = method,
                url = requestUrl,
                responseReader = reader,
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