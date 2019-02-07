package apptentive.com.android.network

import apptentive.com.android.concurrent.Promise
import apptentive.com.android.concurrent.PromiseImpl
import apptentive.com.android.core.toMilliseconds
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.network
import apptentive.com.android.util.StreamUtils
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.GZIPInputStream

class HttpClientImpl(configuration: HttpClientConfiguration) : HttpClient(configuration) {
    /** Queue for sending all the requests */
    private val networkQueue get() = configuration.networkQueue

    /** Queue for dispatching request callbacks */
    private val dispatchQueue get() = configuration.dispatchQueue

    override fun <T : HttpRequest> send(request: T): Promise<T> {
        val promise = PromiseImpl<T>(dispatchQueue)
        networkQueue.dispatch {
            try {
                sendSync(request)
                promise.onValue(request)
            } catch (e: Exception) {
                promise.onError(e)
            }
        }
        return promise
    }

    private fun sendSync(request: HttpRequest) {
        val startTime = System.currentTimeMillis()

        val connection = openConnection(URL(request.url))
        try {
            // request headers
            setupRequestHeaders(connection, request.requestHeaders)

            // request method
            setupRequestMethod(connection, request.method)

            // request body
            writeRequestBody(connection, request)

            // response code
            request.responseCode = connection.responseCode

            // status message
            request.responseMessage = connection.responseMessage

            // response headers
            setupResponseHeaders(connection, request.responseHeaders)

            // response body
            val gzipCompressed = isGzipCompressed(request.responseHeaders)
            if (request.isSuccessful) {
                val responseBody = readResponseBody(connection.inputStream, gzipCompressed)
                request.processResponseBody(responseBody)
            } else {
                val responseBody = readResponseBody(connection.errorStream, gzipCompressed)
                request.errorMessage = String(responseBody)
            }

        } finally {
            connection.disconnect()
        }

        val duration = System.currentTimeMillis() - startTime
        Log.v(network, "Request finished in $duration ms")
    }

    //region Connection

    private fun openConnection(url: URL): HttpURLConnection {
        val connection = url.openConnection() as HttpURLConnection
        connection.connectTimeout = toMilliseconds(configuration.connectTimeout)
        connection.readTimeout = toMilliseconds(configuration.readTimeout)
        return connection
    }

    private fun setupRequestHeaders(connection: HttpURLConnection, requestHeaders: HttpHeaders) {
        for (header in requestHeaders) {
            connection.setRequestProperty(header.key, header.value)
        }
    }

    private fun setupRequestMethod(connection: HttpURLConnection, method: HttpMethod) {
        connection.requestMethod = method.toString()
    }

    private fun writeRequestBody(connection: HttpURLConnection, httpRequest: HttpRequest) {
        val requestBody = httpRequest.createRequestBody()
        if (requestBody != null && requestBody.isNotEmpty()) {
            connection.doOutput = true // this is required for each request with non-empty body
            connection.setChunkedStreamingMode(0) // we know the content length beforehand
            StreamUtils.writeAndClose(connection.outputStream, requestBody)
        }
    }

    private fun setupResponseHeaders(connection: HttpURLConnection, headers: MutableHttpHeaders) {
        for (header in connection.headerFields) {
            headers[header.key] = header.value.toString()
        }
    }

    //endregion

    //region Helpers

    private fun isGzipCompressed(headers: HttpHeaders): Boolean {
        val contentEncoding = headers["Content-Encoding"]
        return contentEncoding != null && contentEncoding.equals("[gzip]", ignoreCase = true)
    }

    private fun readResponseBody(inputStream: InputStream, gzipCompressed: Boolean): ByteArray {
        val stream = if (gzipCompressed) GZIPInputStream(inputStream) else inputStream
        return StreamUtils.readAndClose(stream)
    }

    //endregion
}