package apptentive.com.android.util

import java.util.Locale

internal fun tryFormat(format: String, vararg args: Any?) = try {
    String.format(Locale.US, format, *args)
} catch (e: java.lang.Exception) {
    format
}
