package apptentive.com.android.core

/**
 * Represents an abstract time source object
 */
interface TimeSource {
    /**
     * @return number of seconds since the beginning of the Epoch
     */
    fun getTimeSeconds(): TimeInterval
}

/**
 * Default [TimeSource] implementation
 */
object DefaultTimeSource : TimeSource {
    override fun getTimeSeconds() = toSeconds(System.currentTimeMillis())
}
