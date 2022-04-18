package apptentive.com.android.core

import apptentive.com.android.util.InternalUseOnly

/**
 * Represents an abstract time source object
 */
@InternalUseOnly
interface TimeSource {
    /**
     * @return number of seconds since the beginning of the Epoch
     */
    fun getTimeSeconds(): TimeInterval
}

/**
 * Default [TimeSource] implementation
 */
@InternalUseOnly
object DefaultTimeSource : TimeSource {
    override fun getTimeSeconds() = toSeconds(System.currentTimeMillis())
}
