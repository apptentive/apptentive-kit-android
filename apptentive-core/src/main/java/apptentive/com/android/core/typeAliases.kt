package apptentive.com.android.core

import java.util.TimeZone

typealias Callback = () -> Unit

typealias TimeInterval = Double

internal const val UNDEFINED: Int = -1

internal fun toMilliseconds(time: TimeInterval): Int = (time * 1000L).toInt()
internal fun toSeconds(time: Long): TimeInterval = time * 0.001

fun getTimeSeconds(): TimeInterval = toSeconds(System.currentTimeMillis())
fun getUtcOffset(): Int {
    val timezone = TimeZone.getDefault()
    return timezone.getOffset(System.currentTimeMillis()) / 1000
}

fun isInThePast(time: TimeInterval) = getTimeSeconds() > time

fun TimeInterval.format(digits: Int = 3) = "%.${digits}f".format(this)
