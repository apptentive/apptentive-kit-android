package apptentive.com.android.network

import apptentive.com.android.core.TimeInterval

/**
 * Simple class-container to represent a response from [HttpNetwork]
 */
data class HttpResponse(
    val statusCode: Int,
    val statusMessage: String,
    val content: ByteArray,
    val headers: HttpHeaders,
    val duration: TimeInterval
)