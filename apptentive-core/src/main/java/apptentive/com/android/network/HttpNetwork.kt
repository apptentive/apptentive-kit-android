package apptentive.com.android.network

import android.content.Context
import apptentive.com.android.core.toMilliseconds
import apptentive.com.android.core.toSeconds
import apptentive.com.android.util.NetworkUtils
import java.io.IOException
import java.io.InputStream
import java.lang.IllegalStateException
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.GZIPInputStream

interface HttpNetwork {
    fun performRequest(request: HttpRequest): HttpResponse
}

private class HttpNetworkImpl(context: Context) : HttpNetwork {
    private val contextRef = WeakReference(context.applicationContext)

    override fun performRequest(request: HttpRequest): HttpResponse {
        val startTime = System.currentTimeMillis()

        val connection = openConnection(request)
        try {
            // check network connection
            if (!isNetworkConnected) {
                throw NetworkUnavailableException("Network is not available")
            }

            // request headers
            setRequestHeaders(connection, request.requestHeaders)

            // request method
            setRequestMethod(connection, request.method)

            // request body
            setRequestBody(connection, request)

            // response code
            val responseCode = connection.responseCode
            if (responseCode == -1) {
                throw IOException("Could not retrieve response code from the connection.")
            }

            // status message
            val responseMessage = connection.responseMessage

            // response headers
            val responseHeaders = getResponseHeaders(connection)

            // response
            val responseBody = getResponseBody(connection)

            // duration
            val duration = toSeconds(System.currentTimeMillis() - startTime)
            return HttpResponse(responseCode, responseMessage, responseBody, responseHeaders, duration)
        } finally {
            connection.disconnect()
        }
    }

    //region Connection

    /**
     * Opens [HttpURLConnection] for a given [request]
     */
    private fun openConnection(request: HttpRequest): HttpURLConnection {
        val timeout = toMilliseconds(request.timeout)
        val connection = createConnection(URL(request.url))
        connection.connectTimeout = timeout
        connection.readTimeout = timeout
        connection.useCaches = false
        connection.doInput = true
        return connection
    }

    /**
     * Creates [HttpURLConnection] for a given [url]
     */
    private fun createConnection(url: URL): HttpURLConnection {
        val connection = url.openConnection() as HttpURLConnection
        // workaround for: https://code.google.com/p/android/issues/detail?id=194495
        connection.instanceFollowRedirects = HttpURLConnection.getFollowRedirects()
        return connection
    }

    private fun setRequestHeaders(connection: HttpURLConnection, headers: HttpHeaders) {
        for (header in headers) {
            connection.setRequestProperty(header.name, header.value)
        }
    }

    private fun setRequestMethod(connection: HttpURLConnection, method: HttpMethod) {
        connection.requestMethod = method.toString()
    }

    private fun setRequestBody(connection: HttpURLConnection, request: HttpRequest) {
        val requestBody = request.getRequestBody()
        if (requestBody != null && requestBody.isNotEmpty()) {
            val method = request.method
            if (method == HttpMethod.POST || method == HttpMethod.PUT || method == HttpMethod.PATCH) {
                connection.doOutput = true
                connection.outputStream.write(requestBody)
            } else {
                throw IllegalStateException("Request with HTTP-method $method should not contain body")
            }
        }
    }

    private fun getResponseHeaders(connection: HttpURLConnection): HttpHeaders {
        val headers = MutableHttpHeaders()
        for (header in connection.headerFields) {
            headers[header.key] = header.value.joinToString(separator = ",")
        }
        return headers
    }

    /**
     * Reads connection response fully as an array of bytes.
     */
    private fun getResponseBody(connection: HttpURLConnection): ByteArray {
        return inputStreamForConnection(connection).readBytes()
    }

    /**
     * Returns an input stream to read connection response.
     */
    private fun inputStreamForConnection(connection: HttpURLConnection): InputStream {
        val inputStream = inputStreamForConnectionRespectingContentEncoding(connection)
        return if (isGzipContentEncoding(connection)) GZIPInputStream(inputStream) else inputStream
    }

    /**
     * Picks an appropriate input stream for reading response data from [connection].
     *
     * See https://developer.android.com/reference/java/net/HttpURLConnection#response-handling
     */
    private fun inputStreamForConnectionRespectingContentEncoding(connection: HttpURLConnection): InputStream = try {
        connection.inputStream
    } catch (e: IOException) {
        connection.errorStream
    }

    /**
     * Returns true if connection response is gzip-encoded.
     */
    private fun isGzipContentEncoding(connection: HttpURLConnection): Boolean {
        val contentEncoding = connection.headerFields[HttpHeaders.CONTENT_ENCODING]
        return contentEncoding != null && contentEncoding.contains("gzip")
    }

    //endregion

    //region Helpers

    private val isNetworkConnected get() = NetworkUtils.isNetworkConnected(context)

    private val context get() = contextRef.get()!! // application context should live as long as application lives

    //endregion
}

class NetworkUnavailableException(message: String) : IOException(message)


