package apptentive.com.android.network

import apptentive.com.android.core.TimeInterval
import java.io.InputStream

/**
 * Simple class-container to represent a response from [HttpNetwork]
 */
data class HttpResponse(
    val statusCode: Int,
    val contentLength: Int,
    val content: InputStream?,
    val statusMessage: String,
    val headers: HttpHeaders,
    val duration: TimeInterval
)