package apptentive.com.android

import apptentive.com.android.concurrent.ImmediateExecutorQueue
import apptentive.com.android.core.ApplicationInfo
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.core.ExecutorFactory
import apptentive.com.android.core.Logger
import apptentive.com.android.util.LogLevel
import org.junit.rules.TestWatcher
import org.junit.runner.Description

class DependencyProviderRule(private val enableConsoleOutput: Boolean = false) : TestWatcher() {
    override fun starting(description: Description?) {
        DependencyProvider.register(createPlatformLogger(enableConsoleOutput))
        DependencyProvider.register(createExecutionQueueFactory())
        DependencyProvider.register(createMockApplicationInfo())
    }

    override fun finished(description: Description?) {
        DependencyProvider.clear()
    }
}

private fun createExecutionQueueFactory(): ExecutorFactory = MockExecutorFactory
private fun createPlatformLogger(enableOutput: Boolean) = if (enableOutput) MockLogger else NullLogger
private fun createMockApplicationInfo(): ApplicationInfo = MockApplicationInfo

private object MockExecutorFactory : ExecutorFactory {
    override fun createMainQueue() =
        ImmediateExecutorQueue("main")

    override fun createSerialQueue(name: String) =
        ImmediateExecutorQueue(name)

    override fun createConcurrentQueue(name: String, maxConcurrentTasks: Int?) =
        ImmediateExecutorQueue(name)
}

private object NullLogger : Logger {
    override fun log(logLevel: LogLevel, message: String) {
    }

    override fun log(logLevel: LogLevel, throwable: Throwable) {
    }

    override fun isMainQueue() = false
}

private object MockLogger : Logger {
    override fun log(logLevel: LogLevel, message: String) {
        println(message)
    }

    override fun log(logLevel: LogLevel, throwable: Throwable) {
        throwable.printStackTrace()
    }

    override fun isMainQueue() = false
}

private object MockApplicationInfo : ApplicationInfo {
    override val versionCode = 1000000
    override val versionName = "1.0.0"
}