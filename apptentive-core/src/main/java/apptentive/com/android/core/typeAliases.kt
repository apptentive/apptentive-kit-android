@file:JvmSynthetic
package apptentive.com.android.core

import apptentive.com.android.util.InternalUseOnly
import java.util.TimeZone

@InternalUseOnly
typealias Callback = () -> Unit

@InternalUseOnly
typealias TimeInterval = Double

internal const val UNDEFINED: Int = -1

@InternalUseOnly
fun toMilliseconds(time: TimeInterval): Long = (time * 1000).toLong()

@InternalUseOnly
fun toSeconds(time: Long): TimeInterval = time * 0.001

@InternalUseOnly
fun getTimeSeconds(): TimeInterval = toSeconds(System.currentTimeMillis())

@InternalUseOnly
fun getUtcOffset(): Int {
    val timezone = TimeZone.getDefault()
    return timezone.getOffset(System.currentTimeMillis()) / 1000
}

@InternalUseOnly
fun isInThePast(time: TimeInterval): Boolean = getTimeSeconds() > time

@InternalUseOnly
fun TimeInterval.format(digits: Int = 3): String = "%.${digits}f".format(this)
