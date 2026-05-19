package apptentive.com.android.feedback

import apptentive.com.android.core.Logger
import apptentive.com.android.core.Provider
import apptentive.com.android.util.LogLevel

class MockAndroidLoggerProvider : Provider<Logger> {
    private val logger by lazy { MockAndroidLogger() }

    override fun get(): Logger = logger
}

class MockAndroidLogger : Logger {
    override fun isMainQueue(): Boolean = true

    override fun log(logLevel: LogLevel, throwable: Throwable) {
    }

    override fun log(logLevel: LogLevel, message: String) {
    }
}
