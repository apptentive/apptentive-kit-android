package apptentive.com.android.core

import android.os.Looper
import apptentive.com.android.util.LogLevel
import java.io.PrintWriter
import java.io.StringWriter

interface Logger {
    fun log(logLevel: LogLevel, message: String)
    fun log(logLevel: LogLevel, throwable: Throwable)
    fun isMainQueue(): Boolean
}

// TODO: rename to AndroidLoggerProvider
class DefaultLoggerProvider(tag: String) : Provider<Logger> {
    private val logger by lazy { DefaultLogger(tag) }

    override fun get(): Logger = logger
}

// TODO: rename to AndroidLogger
private class DefaultLogger(val tag: String) : Logger {
    override fun log(logLevel: LogLevel, message: String) {
        android.util.Log.println(logLevel.ordinal, tag, message)
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
}