package apptentive.com.android.network

import apptentive.com.android.core.TimeInterval
import apptentive.com.android.util.InternalUseOnly

/**
 * A container to hold a generic response from [HttpNetwork]
 * @param statusCode HTTP-status code
 * @param statusMessage HTTP-status message
 * @param data raw connection response
 * @param headers HTTP-response headers
 * @param duration duration of the request
 */
@InternalUseOnly
data class HttpNetworkResponse(
    val statusCode: Int,
    val statusMessage: String,
    val data: ByteArray,
    val headers: HttpHeaders,
    val duration: TimeInterval
) {
    fun asString(): String = data.toString(Charsets.UTF_8)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HttpNetworkResponse

        if (statusCode != other.statusCode) return false
        if (statusMessage != other.statusMessage) return false
        if (!data.contentEquals(other.data)) return false
        if (headers != other.headers) return false
        if (duration != other.duration) return false

        return true
    }

    override fun hashCode(): Int {
        var result = statusCode
        result = 31 * result + statusMessage.hashCode()
        result = 31 * result + data.contentHashCode()
        result = 31 * result + headers.hashCode()
        result = 31 * result + duration.hashCode()
        return result
    }
}
