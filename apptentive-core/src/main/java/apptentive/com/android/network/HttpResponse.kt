package apptentive.com.android.network

import apptentive.com.android.core.TimeInterval

/**
 * A container which holds a typed response from a HTTP-request.
 */
data class HttpResponse<T>(
    val statusCode: Int,
    val statusMessage: String,
    val content: T, // TODO: find a better name
    val headers: HttpHeaders,
    val duration: TimeInterval
)