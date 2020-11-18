package apptentive.com.android.util

import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.core.Logger

enum class LogLevel {
    Verbose,
    Debug,
    Info,
    Warning,
    Error
}

object Log {
    var logLevel: LogLevel = LogLevel.Info
    private val logger = DependencyProvider.of<Logger>()

    fun v(tag: LogTag, message: String) = log(LogLevel.Verbose, tag, message)
    fun d(tag: LogTag, message: String) = log(LogLevel.Debug, tag, message)
    fun i(tag: LogTag, message: String) = log(LogLevel.Info, tag, message)
    fun w(tag: LogTag, message: String) = log(LogLevel.Warning, tag, message)
    fun e(tag: LogTag, message: String) = log(LogLevel.Error, tag, message)
    fun e(tag: LogTag, message: String, e: Throwable) = log(LogLevel.Error, tag, message, e)

    private fun log(level: LogLevel, tag: LogTag, message: String, throwable: Throwable? = null) {
        if (!canLog(level)) {
            return
        }

        val buffer = StringBuilder()

        // thread name
        if (!logger.isMainQueue()) {
            buffer.append('[')
            buffer.append(Thread.currentThread().name)
            buffer.append(']')
        }

        // tag
        buffer.append(" [")
        buffer.append(tag.name)
        buffer.append("] ")

        // message
        buffer.append(message)

        // output
        logger.log(level, buffer.toString())

        // throwable
        if (throwable != null) {
            logger.log(level, throwable)
        }
    }

    fun canLog(level: LogLevel): Boolean {
        return level.ordinal >= logLevel.ordinal
    }
}

data class LogTag(val name: String) {
    override fun toString(): String = name
}