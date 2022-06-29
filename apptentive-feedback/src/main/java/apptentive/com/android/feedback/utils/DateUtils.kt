package apptentive.com.android.feedback.utils

import apptentive.com.android.core.TimeInterval
import apptentive.com.android.core.toMilliseconds
import apptentive.com.android.util.InternalUseOnly
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@InternalUseOnly
fun convertToDate(timeInterval: TimeInterval, pattern: String = "MM/dd/yyyy"): String {
    val dateFormat = SimpleDateFormat(pattern, Locale.US)
    // Server returns TimeIntervals in seconds.
    return dateFormat.format(Date(toMilliseconds(timeInterval)))
}

@InternalUseOnly
fun convertToGroupDate(timeInterval: TimeInterval): String {
    // Server returns TimeIntervals in seconds.
    val createdAt = toMilliseconds(timeInterval)

    val DAY_IN_MILLIS = 24 * 60 * 60 * 1000L

    val currentDate = Calendar.getInstance()
    val groupDate = Calendar.getInstance().apply { timeInMillis = createdAt }

    return when {
        groupDate.get(Calendar.YEAR) < currentDate.get(Calendar.YEAR) -> {
            val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
            dateFormat.format(Date(createdAt))
        }

        createdAt < currentDate.timeInMillis - (DAY_IN_MILLIS * 6) -> {
            val dateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())
            dateFormat.format(Date(createdAt))
        }

        else -> {
            val dateFormat = SimpleDateFormat("EEEE MM/dd", Locale.getDefault())
            dateFormat.format(Date(createdAt))
        }
    }
}
