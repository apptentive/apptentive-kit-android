package apptentive.com.android.util

import androidx.annotation.VisibleForTesting
import apptentive.com.android.core.AndroidLoggerProvider
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.core.Logger

enum class LogLevel {
    Verbose,
    Debug,
    Info,
    Warning,
    Error
}

@InternalUseOnly
object Log {
    var logLevel: LogLevel = LogLevel.Info
    private const val CHUNK_LOG_MESSAGE_LENGTH = 3900 // Max length is 4068. 3900 gives room for additional info
    private val logger: Logger = try {
        DependencyProvider.of()
    } catch (e: Exception) {
        android.util.Log.w("Apptentive", "Failed to get logger from DependencyProvider. Registering and using fallback.")
        val loggerProvider = AndroidLoggerProvider("Apptentive")
        DependencyProvider.register(loggerProvider)
        loggerProvider.get()
    }

    @JvmStatic fun v(tag: LogTag, message: String) = log(LogLevel.Verbose, tag, message)
    @JvmStatic fun d(tag: LogTag, message: String) = log(LogLevel.Debug, tag, message)
    @JvmStatic fun i(tag: LogTag, message: String) = log(LogLevel.Info, tag, message)
    @JvmStatic fun w(tag: LogTag, message: String) = log(LogLevel.Warning, tag, message)
    @JvmStatic fun e(tag: LogTag, message: String) = log(LogLevel.Error, tag, message)
    @JvmStatic fun e(tag: LogTag, message: String, e: Throwable?) = log(LogLevel.Error, tag, message, e)

    // For legacy java code
    @JvmStatic fun v(tag: LogTag, format: String, vararg args: Any?) = log(LogLevel.Verbose, tag, tryFormat(format, *args))
    @JvmStatic fun d(tag: LogTag, format: String, vararg args: Any?) = log(LogLevel.Debug, tag, tryFormat(format, *args))
    @JvmStatic fun w(tag: LogTag, format: String, vararg args: Any?) = log(LogLevel.Warning, tag, tryFormat(format, *args))
    @JvmStatic fun e(tag: LogTag, e: Throwable, format: String, vararg args: Any?) = log(LogLevel.Error, tag, tryFormat(format, *args), e)

    private fun log(level: LogLevel, tag: LogTag, message: String, throwable: Throwable? = null) {
        if (!canLog(level)) {
            return
        }

        val messagesWithTags = getMessagesWithTags(message, tag)

        // output

        messagesWithTags.forEach { messageWithTag ->
            logger.log(level, messageWithTag)
        }

        // throwable
        if (throwable != null) {
            logger.log(level, throwable)
        }
    }

    /**
     * Constructs a log message with thread tag and log tag appended with the pattern
     * "[Thread Name Log] [Log Tag Name] Message"
     *
     * e.g. [Main Queue] [SYSTEM] Registration successful
     *
     *
     * If tags + message is too long for Logcat (over 4068 characters), chunk into messages it
     * can handle with the pattern
     * "[Thread Name Log] [Log Tag Name] [Message Chunk Current/Message Chunks Total] Message"
     *
     * e.g. [SDK Queue] [ENGAGEMENT MANIFEST] [1/21] EngagementManifest...
     *
     */
    @VisibleForTesting
    fun getMessagesWithTags(message: String, tag: LogTag): List<String> {
        val messages = message.chunked(CHUNK_LOG_MESSAGE_LENGTH)

        // thread name
        val threadTag = "[${if (logger.isMainQueue()) "Main Queue" else Thread.currentThread().name}]"

        // tag name
        val logTag = " [${tag.name}]"

        return arrayListOf<String>().apply {
            messages.forEachIndexed { index, message ->
                add(
                    buildString {
                        append(threadTag)
                        append(logTag)

                        if (messages.size > 1) {
                            val chunkIndex = "[${index + 1}/${messages.size}]" // e.g. "[1/2]"
                            append(" $chunkIndex")
                        }

                        append(" $message")
                    }
                )
            }
        }
    }

    @JvmStatic fun canLog(level: LogLevel): Boolean {
        return level.ordinal >= logLevel.ordinal
    }
}

@InternalUseOnly
data class LogTag(val name: String) {
    override fun toString(): String = name
}
