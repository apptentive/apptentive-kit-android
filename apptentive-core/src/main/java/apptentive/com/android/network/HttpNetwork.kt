package apptentive.com.android.network

import android.content.Context
import apptentive.com.android.core.TimeInterval
import apptentive.com.android.core.toMilliseconds
import apptentive.com.android.core.toSeconds
import apptentive.com.android.network.Constants.DEFAULT_REQUEST_TIMEOUT
import apptentive.com.android.util.NetworkUtils
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.GZIPInputStream

/**
 * Represents a basic network operation and network state query.
 */
interface HttpNetwork {
    fun isNetworkConnected(): Boolean
    fun performRequest(request: HttpRequest<*>): HttpNetworkResponse
}

class DefaultHttpNetwork(
    context: Context,
    private val connectTimeout: TimeInterval = DEFAULT_REQUEST_TIMEOUT,
    private val readTimeout: TimeInterval = DEFAULT_REQUEST_TIMEOUT
) : HttpNetwork {
    private val applicationContext: Context = context.applicationContext

    override fun isNetworkConnected() = NetworkUtils.isNetworkConnected(applicationContext)

    override fun performRequest(request: HttpRequest<*>): HttpNetworkResponse {
        val startTime = System.currentTimeMillis()

        val connection = openConnection(request)
        try {
            // request headers
            setRequestHeaders(connection, request.headers)

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
            val stream = getResponseStream(connection)

            // duration
            val duration = toSeconds(System.currentTimeMillis() - startTime)
            return HttpNetworkResponse(
                statusCode = responseCode,
                statusMessage = responseMessage,
                data = stream.readBytes(),
                headers = responseHeaders,
                duration = duration
            ).also { stream.close() }
        } finally {
            connection.disconnect()
        }
    }

    //region Connection

    /**
     * Opens [HttpURLConnection] for a given [request]
     */
    private fun openConnection(request: HttpRequest<*>): HttpURLConnection {
        val connection = createConnection(request.url)
        connection.connectTimeout = toMilliseconds(connectTimeout)
        connection.readTimeout = toMilliseconds(readTimeout)
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

    private fun setRequestBody(connection: HttpURLConnection, request: HttpRequest<*>) {
        val requestBody = request.requestBody
        if (requestBody != null) {
            connection.doOutput = true
            connection.setRequestProperty(HttpHeaders.CONTENT_TYPE, requestBody.contentType)
            requestBody.write(connection.outputStream)
        }
    }

    private fun getResponseHeaders(connection: HttpURLConnection): HttpHeaders {
        val headers = MutableHttpHeaders()
        for (header in connection.headerFields) {
            val name = header.key ?: continue // HttpUrlConnection includes the status line as a header with a null key
            val values = header.value
            headers[name] = values.joinToString(separator = ",")
        }
        return headers
    }

    /**
     * Reads connection response fully as an array of bytes.
     */
    private fun getResponseStream(connection: HttpURLConnection): InputStream {
        return inputStreamForConnection(connection)
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
    private fun inputStreamForConnectionRespectingContentEncoding(connection: HttpURLConnection): InputStream =
        try {
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
}
