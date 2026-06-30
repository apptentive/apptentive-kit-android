package apptentive.com.android.util

object OsUtils {
    val isWindows = System.getProperty("os.name")
        ?.lowercase()
        ?.contains("windows") == true
}
