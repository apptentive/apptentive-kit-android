package apptentive.com.android.network

import apptentive.com.android.core.TimeInterval

/**
 * A container which holds a typed response from a HTTP-request.
 */
data class HttpResponse<T>(
    val statusCode: Int,
    val statusMessage: String,
    val payload: T,
    val headers: HttpHeaders,
    val duration: TimeInterval
)