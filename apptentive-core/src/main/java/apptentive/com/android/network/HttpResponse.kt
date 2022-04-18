package apptentive.com.android.network

import apptentive.com.android.core.TimeInterval
import apptentive.com.android.util.InternalUseOnly

/**
 * A container to hold a typed response for [HttpRequest]
 *
 * @param statusCode HTTP-status code
 * @param statusMessage HTTP-status message
 * @param payload HTTP-response payload object
 * @param headers HTTP-response headers
 * @param duration duration of the request
 */
@InternalUseOnly
data class HttpResponse<T>(
    val statusCode: Int,
    val statusMessage: String,
    val payload: T,
    val headers: HttpHeaders,
    val duration: TimeInterval
)
