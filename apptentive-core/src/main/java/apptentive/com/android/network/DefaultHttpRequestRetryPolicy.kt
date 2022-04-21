package apptentive.com.android.network

import apptentive.com.android.core.TimeInterval
import apptentive.com.android.core.UNDEFINED
import apptentive.com.android.util.InternalUseOnly
import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random

/**
 * Default retry policy for HTTP-request (will be used unless overwritten).
 */
@InternalUseOnly
class DefaultHttpRequestRetryPolicy(
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

        return numRetries < maxNumRetries
    }

    override fun getRetryDelay(numRetries: Int): TimeInterval {
        // exponential back-off
        val temp = min(MAX_RETRY_CAP, retryDelay * 2.0.pow((numRetries).toDouble()))
        return temp / 2 * (1.0 + Random.nextDouble())
    }

    companion object {
        /**
         * Maximum retry timeout for the exponential back-off
         */
        private const val MAX_RETRY_CAP: TimeInterval = 10 * 60.0
    }
}
