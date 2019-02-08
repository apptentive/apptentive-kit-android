package apptentive.com.android.network

import apptentive.com.android.core.toMilliseconds
import apptentive.com.android.core.toSeconds
import apptentive.com.android.util.StreamUtils
import java.io.FilterInputStream
import java.io.IOException
import java.io.InputStream
import java.lang.IllegalStateException
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.GZIPInputStream

class HttpNetworkException(message: String, throwable: Throwable) : Exception(message, throwable)

interface HttpNetwork {
    @Throws(HttpNetworkException::class)
    fun performRequest(request: HttpRequest): HttpResponse
}


private class HttpNetworkImpl : HttpNetwork {
    override fun performRequest(request: HttpRequest): HttpResponse {
        val startTime = System.currentTimeMillis()

        val connection = openConnection(request)
        var keepConnectionOpen = false
        try {
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

            val responseBody: ByteArray
            val errorMessage: String

            // response body
            keepConnectionOpen = true
            val duration = toSeconds(System.currentTimeMillis() - startTime)
            return HttpResponse(responseCode, connection.contentLength, HttpUrlConnectionStream(connection), responseMessage, responseHeaders, duration)

            // need to keep connection open until the caller reads data from the stream

        } finally {
            if (!keepConnectionOpen) {
                connection.disconnect()
            }
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
                StreamUtils.writeAndClose(connection.outputStream, requestBody)
            } else {
                throw IllegalStateException("Request with HTTP-method $method should not contain body")
            }
        }
    }

    private fun getResponseHeaders(connection: HttpURLConnection): HttpHeaders {
        val headers = MutableHttpHeaders()
        for (header in connection.headerFields) {
            headers[header.key] = header.value.toString()
        }
        return headers
    }

    //endregion
}

private class HttpUrlConnectionStream(private val connection: HttpURLConnection) :
    FilterInputStream(inputStreamForConnection(connection)) {

    override fun close() {
        super.close()
        connection.disconnect()
    }

    companion object {
        private fun inputStreamForConnection(connection: HttpURLConnection) : InputStream {
            val inputStream = inputStreamForConnectionBase(connection)
            val compressed = isResponseCompressed(connection)
            return if (compressed) GZIPInputStream(inputStream) else inputStream
        }

        private fun inputStreamForConnectionBase(connection: HttpURLConnection): InputStream = try {
            connection.inputStream
        } catch (e: IOException) {
            connection.errorStream
        }

        private fun isResponseCompressed(connection: HttpURLConnection): Boolean {
            val contentEncoding = connection.getHeaderField(HttpHeaders.CONTENT_ENCODING)
            return contentEncoding != null && contentEncoding.equals("gzip", true)
        }
    }
}


