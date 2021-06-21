package apptentive.com.android.core

import android.os.Looper
import apptentive.com.android.util.LogLevel
import java.io.PrintWriter
import java.io.StringWriter

/** Interface which represents basic logging operations:
 * • writing log message
 * • writing exception stacktrace
 * • checking if this log statement belongs to the main thread
 */
interface Logger {
    fun log(logLevel: LogLevel, message: String)
    fun log(logLevel: LogLevel, throwable: Throwable)
    fun isMainQueue(): Boolean
}

/** Provider-class for a platform-specific logger implementation */
class AndroidLoggerProvider(tag: String) : Provider<Logger> {
    private val logger by lazy { AndroidLogger(tag) }

    override fun get(): Logger = logger
}

/** Android-specific logger implementation */
private class AndroidLogger(val tag: String) : Logger {
    override fun log(logLevel: LogLevel, message: String) {
        android.util.Log.println(getLogPriority(logLevel), tag, message)
    }

    override fun log(logLevel: LogLevel, throwable: Throwable) {
        log(logLevel, getStackTrace(throwable))
    }

    override fun isMainQueue(): Boolean = Looper.getMainLooper() == Looper.myLooper()

    private fun getStackTrace(throwable: Throwable): String {
        val writer = StringWriter()
        throwable.printStackTrace(PrintWriter(writer))
        return writer.toString()
    }

    private companion object {
        fun getLogPriority(logLevel: LogLevel): Int {
            return when (logLevel) {
                LogLevel.Verbose -> android.util.Log.VERBOSE
                LogLevel.Debug -> android.util.Log.DEBUG
                LogLevel.Info -> android.util.Log.INFO
                LogLevel.Warning -> android.util.Log.WARN
                LogLevel.Error -> android.util.Log.ERROR
            }
        }
    }
}
