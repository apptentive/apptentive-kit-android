package apptentive.com.android.network

import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

class HttpRequest(val url: String, val method: HttpMethod = HttpMethod.GET) {
    private val requestHeaders: HttpHeaders = HttpHeaders()

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
                setupRequestBody(connection, requestBody)
            }

            // response
            val responseCode = connection.responseCode
            if (isValidResponseCode(responseCode)) {

            } else {

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
        for (header in headers.entries) {
            connection.setRequestProperty(header.key, header.value)
        }
    }

    private fun setupRequestMethod(connection: HttpURLConnection, method: HttpMethod) {
        connection.requestMethod = method.toString()
    }

    private fun setupRequestBody(connection: HttpURLConnection, requestBody: ByteArray) {
        connection.doOutput = true // this is required for each request with non-empty body
        connection.setChunkedStreamingMode(0) // we know the content length before hand

        var outputStream: OutputStream? = null
        try {
            outputStream = connection.outputStream
            outputStream.write(requestBody)
        } finally {
            outputStream?.close()
        }
    }

    private fun isValidResponseCode(responseCode: Int): Boolean =
        responseCode >= HttpURLConnection.HTTP_OK && responseCode < HttpURLConnection.HTTP_MULT_CHOICE

    //endregion

    //region Request Body

    protected fun createRequestBody(): ByteArray? {
        return null
    }

    //endregion
}