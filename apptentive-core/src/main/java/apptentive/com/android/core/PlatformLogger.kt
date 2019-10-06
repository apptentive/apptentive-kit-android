package apptentive.com.android.core

import apptentive.com.android.concurrent.ExecutorQueue
import apptentive.com.android.util.LogLevel
import java.io.PrintWriter
import java.io.StringWriter

interface PlatformLogger {
    fun log(logLevel: LogLevel, message: String)
    fun log(logLevel: LogLevel, throwable: Throwable)
    fun isMainQueue(): Boolean
}

class DefaultLoggerProvider(tag: String) : Provider<PlatformLogger> {
    private val logger by lazy { DefaultPlatformLogger(tag) }

    override fun get(): PlatformLogger = logger
}

private class DefaultPlatformLogger(val tag: String) : PlatformLogger {
    override fun log(logLevel: LogLevel, message: String) {
        android.util.Log.println(logLevel.ordinal, tag, message)
    }

    override fun log(logLevel: LogLevel, throwable: Throwable) {
        log(logLevel, getStackTrace(throwable))
    }

    override fun isMainQueue(): Boolean = ExecutorQueue.isMainQueue

    private fun getStackTrace(throwable: Throwable): String {
        val writer = StringWriter()
        throwable.printStackTrace(PrintWriter(writer))
        return writer.toString()
    }
}