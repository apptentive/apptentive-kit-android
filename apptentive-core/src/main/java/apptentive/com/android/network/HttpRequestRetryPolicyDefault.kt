package apptentive.com.android.network

import apptentive.com.android.core.TimeInterval
import apptentive.com.android.core.UNDEFINED
import kotlin.math.min
import kotlin.random.Random

/**
 * Default retry policy for HTTP-request (will be used unless overwritten).
 */
class HttpRequestRetryPolicyDefault(
    private val maxNumRetries: Int = Constants.DEFAULT_RETRY_MAX_COUNT,
    private val retryDelay: TimeInterval = Constants.DEFAULT_RETRY_DELAY
) : HttpRequestRetryPolicy {
    override fun shouldRetry(statusCode: Int, numRetries: Int): Boolean {
        if (statusCode in 400..499) {
            return false // don't retry if request was unauthorized or rejected
        }

        if (maxNumRetries == UNDEFINED) {
            return true // retry indefinitely
        }

        return numRetries <= maxNumRetries
    }

    override fun getRetryDelay(numRetries: Int): TimeInterval {
        // exponential back-off
        val temp = min(MAX_RETRY_CAP, retryDelay * Math.pow(2.0, (numRetries - 1).toDouble()))
        return temp / 2 * (1.0 + Random.nextDouble())
    }

    companion object {
        /**
         * Maximum retry timeout for the exponential back-off
         */
        private const val MAX_RETRY_CAP: TimeInterval = 10 * 60.0
    }
}