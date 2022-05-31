package apptentive.com.android.feedback.utils

import apptentive.com.android.core.TimeInterval
import apptentive.com.android.util.InternalUseOnly
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@InternalUseOnly
fun convertToDate(timeInterval: TimeInterval, pattern: String = "MM/dd/yyyy"): String {
    val dateFormat = SimpleDateFormat(pattern, Locale.US)
    return dateFormat.format(Date(timeInterval.toLong() * 1000))
}
