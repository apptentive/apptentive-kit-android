package apptentive.com.android.network

import apptentive.com.android.util.StreamUtils
import apptentive.com.android.util.StreamUtils.readBytes
import apptentive.com.android.util.StreamUtils.writeBytes
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.GZIPInputStream

class HttpRequest(val url: String, val method: HttpMethod = HttpMethod.GET) {
    private val requestHeaders: MutableHttpHeaders = MutableHttpHeaders()
    private val responseHeaders: MutableHttpHeaders = MutableHttpHeaders()

    protected var responseData: ByteArray = ByteArray(0)
        private set

    internal fun sendRequestSync() {
        val connection = openConnection(URL(url))
        try {
            // request headers
            setupRequestHeaders(connection, requestHeaders)

            // request method
            setupRequestMethod(connection, method)

            // request body
            val requestBody = createRequestBody()
            if (requestBody != null && requestBody.isNotEmpty()) {
                writeRequestBody(connection, requestBody)
            }

            // response code
            val responseCode = connection.responseCode

            // response headers
            setupResponseHeaders(connection, responseHeaders)

            // response data
            val gzipCompressed = isGzipCompressed(responseHeaders)
            if (isValidResponseCode(responseCode)) {
                responseData = readResponseBody(connection.inputStream, gzipCompressed)
            } else {
                responseData = readResponseBody(connection.errorStream, gzipCompressed)
            }

        } finally {
            connection.disconnect()
        }
    }

    //region Connection

    private fun openConnection(url: URL): HttpURLConnection {
        val connection = url.openConnection() as HttpURLConnection
        connection.connectTimeout = 45000 // FIXME: make it configurable
        connection.connectTimeout = 45000 // FIXME: make it configurable
        return connection
    }

    private fun setupRequestHeaders(connection: HttpURLConnection, headers: HttpHeaders) {
        for (header in headers) {
            connection.setRequestProperty(header.key, header.value)
        }
    }

    private fun setupRequestMethod(connection: HttpURLConnection, method: HttpMethod) {
        connection.requestMethod = method.toString()
    }

    private fun writeRequestBody(connection: HttpURLConnection, requestBody: ByteArray) {
        connection.doOutput = true // this is required for each request with non-empty body
        connection.setChunkedStreamingMode(0) // we know the content length beforehand
        writeBytes(connection.outputStream, requestBody)
    }

    private fun setupResponseHeaders(connection: HttpURLConnection, headers: MutableHttpHeaders) {
        for (header in connection.headerFields) {
            headers[header.key] = header.value.toString()
        }
    }

    private fun readResponseBody(inputStream: InputStream, gzipCompressed: Boolean): ByteArray {
        return readBytes(if (gzipCompressed) GZIPInputStream(inputStream) else inputStream)
    }

    //endregion

    //region Request Body

    protected fun createRequestBody(): ByteArray? {
        return null
    }

    //endregion

    //region Helpers

    companion object {
        private fun isGzipCompressed(headers: HttpHeaders): Boolean {
            val contentEncoding = headers["Content-Encoding"]
            return contentEncoding != null && contentEncoding.equals("[gzip]", ignoreCase = true)
        }

        private fun isValidResponseCode(responseCode: Int): Boolean =
            responseCode >= HttpURLConnection.HTTP_OK && responseCode < HttpURLConnection.HTTP_MULT_CHOICE
    }

    //endregion
}