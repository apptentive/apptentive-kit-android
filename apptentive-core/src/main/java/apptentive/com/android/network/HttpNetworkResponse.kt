package apptentive.com.android.network

import apptentive.com.android.core.TimeInterval
import java.io.Closeable
import java.io.InputStream

/**
 * A container to hold a generic response from [HttpNetwork]
 * @param statusCode HTTP-status code
 * @param statusMessage HTTP-status message
 * @param data raw connection response
 * @param headers HTTP-response headers
 * @param duration duration of the request
 */
data class HttpNetworkResponse(
    val statusCode: Int,
    val statusMessage: String,
    val data: ByteArray,
    val headers: HttpHeaders,
    val duration: TimeInterval
) {
    fun asString(): String = data.toString(Charsets.UTF_8)
}