@file:JvmSynthetic
package apptentive.com.android.core

import java.util.TimeZone

typealias Callback = () -> Unit

internal typealias TimeInterval = Double

internal const val UNDEFINED: Int = -1

internal fun toMilliseconds(time: TimeInterval): Long = (time * 1000).toLong()

internal fun toSeconds(time: Long): TimeInterval = time * 0.001

internal fun getTimeSeconds(): TimeInterval = toSeconds(System.currentTimeMillis())

internal fun getUtcOffset(): Int {
    val timezone = TimeZone.getDefault()
    return timezone.getOffset(System.currentTimeMillis()) / 1000
}

internal fun isInThePast(time: TimeInterval): Boolean = getTimeSeconds() > time

internal fun TimeInterval.format(digits: Int = 3): String = "%.${digits}f".format(this)
