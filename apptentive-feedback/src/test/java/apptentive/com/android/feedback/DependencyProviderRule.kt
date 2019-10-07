package apptentive.com.android.feedback

import apptentive.com.android.concurrent.ImmediateExecutorQueue
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.core.ExecutorQueueFactory
import apptentive.com.android.core.PlatformLogger
import apptentive.com.android.util.LogLevel
import org.junit.rules.TestWatcher
import org.junit.runner.Description

class DependencyProviderRule : TestWatcher() {
    override fun starting(description: Description?) {
        DependencyProvider.register(createPlatformLogger())
        DependencyProvider.register(createExecutionQueueFactory())
    }

    override fun finished(description: Description?) {
        DependencyProvider.clear()
    }
}

private fun createExecutionQueueFactory(): ExecutorQueueFactory = MockExecutorQueueFactory(false)
private fun createPlatformLogger(): PlatformLogger = MockPlatformLogger()

private class MockExecutorQueueFactory(val poseAsMainQueue: Boolean) : ExecutorQueueFactory {
    override fun createMainQueue() = ImmediateExecutorQueue("main")
    override fun createSerialQueue(name: String) = ImmediateExecutorQueue(name)
    override fun createConcurrentQueue(name: String, maxConcurrentTasks: Int) =
        ImmediateExecutorQueue(name)

    override fun isMainQueue() = poseAsMainQueue
}

private class MockPlatformLogger(
) : PlatformLogger {
    override fun log(logLevel: LogLevel, message: String) {
        print(message)
    }

    override fun log(logLevel: LogLevel, throwable: Throwable) {
        throwable.printStackTrace()
    }
}