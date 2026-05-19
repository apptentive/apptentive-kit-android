package apptentive.com.android.core

import apptentive.com.android.util.toSeconds

/**
 * Represents an abstract time source object
 */
internal interface TimeSource {
    /**
     * @return number of seconds since the beginning of the Epoch
     */
    fun getTimeSeconds(): TimeInterval
}

/**
 * Default [TimeSource] implementation
 */
internal object DefaultTimeSource : TimeSource {
    override fun getTimeSeconds() = toSeconds(System.currentTimeMillis())
}
