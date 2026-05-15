package apptentive.com.android.core.network

import apptentive.com.android.core.TimeInterval

/**
 * Retry policy for HTTP-request.
 */
internal interface HttpRequestRetryPolicy {
    /**
     * Determines if request should be retried.
     * @param statusCode HTTP-status code of the request.
     * @param numRetries number of times the request was already retried.
     */
    fun shouldRetry(statusCode: Int, numRetries: Int): Boolean

    /**
     * Returns a delay for the next retry.
     * @param numRetries number of times the request was already retried.
     */
    fun getRetryDelay(numRetries: Int): TimeInterval
}
