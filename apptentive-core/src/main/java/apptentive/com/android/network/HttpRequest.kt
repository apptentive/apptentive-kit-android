package apptentive.com.android.network

import apptentive.com.android.util.InternalUseOnly
import java.net.URL

/**
 * Base class for HTTP-requests.
 *
 * @param method HTTP-request method.
 * @param url URL
 * @param headers HTTP-request headers
 * @param requestBody optional request body.
 * @param responseReader deserializer responsible for converting raw response stream to a typed response object.
 * @param tag optional tag for request identification.
 * @param userData optional user data associated with the request.
 */
@InternalUseOnly
class HttpRequest<T> private constructor(
    val method: HttpMethod,
    val url: URL,
    val headers: HttpHeaders,
    val requestBody: HttpRequestBody?,
    val responseReader: HttpResponseReader<T>,
    val tag: String? = null,
    val userData: Any? = null

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
    internal fun readResponseObject(response: HttpNetworkResponse): T {
        return responseReader.read(response)
    }

    //endregion

    //region Builder

    /**
     * Builder class for creating [HttpRequest] instances.
     */
    @InternalUseOnly
    data class Builder<T>(val url: URL) {
        private val headers = MutableHttpHeaders()
        private lateinit var reader: HttpResponseReader<T>
        private var method = HttpMethod.GET
        private var requestBody: HttpRequestBody? = null
        private var tag: String? = null
        private var userData: Any? = null

        constructor(url: String) : this(URL(url))

        //region Method

        /** Sets GET method */
        fun get() = method(HttpMethod.GET, null)

        /** Sets POST method with optional request body */
        fun post(body: HttpRequestBody? = null) = method(HttpMethod.POST, body)

        /** Sets PUT method with optional request body */
        fun put(body: HttpRequestBody? = null) = method(HttpMethod.PUT, body)

        /** Sets DELETE method with optional request body */
        fun delete(body: HttpRequestBody? = null) = method(HttpMethod.DELETE, body)

        /** Sets PATCH method with optional request body */
        fun patch(body: HttpRequestBody? = null) = method(HttpMethod.PATCH, body)

        fun method(method: HttpMethod, body: ByteArray, contentType: String): Builder<T> {
            return method(
                method = method,
                requestBody = BinaryRequestBody(
                    data = body,
                    contentType = contentType
                )
            )
        }

        fun method(method: HttpMethod, body: Any? = null): Builder<T> {
            return method(
                method = method,
                requestBody = if (body != null) HttpJsonRequestBody(body) else null
            )
        }

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
            if (!this::reader.isInitialized) {
                throw IllegalStateException("Builder is missing a response reader")
            }

            return HttpRequest(
                method = method,
                url = url,
                responseReader = reader,
                requestBody = requestBody,
                headers = headers,
                tag = tag,
                userData = userData
            )
        }

        //endregion
    }
    //endregion
}
