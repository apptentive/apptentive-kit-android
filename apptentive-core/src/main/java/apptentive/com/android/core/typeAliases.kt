package apptentive.com.android.core

typealias TimeInterval = Double

fun toMilliseconds(time: TimeInterval) : Int = (time * 1000L).toInt()