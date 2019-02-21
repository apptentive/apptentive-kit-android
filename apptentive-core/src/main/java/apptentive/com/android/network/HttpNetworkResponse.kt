package apptentive.com.android.network

import apptentive.com.android.core.TimeInterval

/**
 * A container to hold a generic (bytes only) response from [HttpNetwork]
 */
@Suppress("ArrayInDataClass")
data class HttpNetworkResponse(
    val statusCode: Int,
    val statusMessage: String,
    val payload: ByteArray,
    val headers: HttpHeaders,
    val duration: TimeInterval
)