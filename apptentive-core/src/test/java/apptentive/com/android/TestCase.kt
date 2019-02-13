package apptentive.com.android

import apptentive.com.android.concurrent.ImmediateExecutionQueue
import apptentive.com.android.core.ExecutionQueueFactory
import apptentive.com.android.core.PlatformLogger
import apptentive.com.android.core.Provider
import apptentive.com.android.util.LogLevel
import org.junit.After
import org.junit.Assert.*
import org.junit.Before

open class TestCase(
    private val logMessages: Boolean = false,
    private val logStackTraces: Boolean = false
) {
    private val results = mutableListOf<Any>()

    // Before/After

    @Before
    open fun setUp() {
        Provider.register(createPlatformLogger())
        Provider.register(createExecutionQueueFactory())
        results.clear()
    }

    @After
    open fun tearDown() {
        Provider.clear()
    }

    //endregion

    //region Factory

    private fun createExecutionQueueFactory(): ExecutionQueueFactory = MockExecutionQueueFactory(false)
    private fun createPlatformLogger(): PlatformLogger = MockPlatformLogger(logMessages, logStackTraces)

    //endregion

    //region Results

    protected fun addResult(result: Any) {
        results.add(result)
    }

    protected fun assertResults(vararg expected: Any, clearResults: Boolean = true) {
        assertEquals(expected.toList(), results)
        if (clearResults) {
            results.clear()
        }
    }

    //endregion
}

private class MockExecutionQueueFactory(val poseAsMainQueue: Boolean) : ExecutionQueueFactory {
    override fun createMainQueue() = ImmediateExecutionQueue("main")
    override fun createSerialQueue(name: String) = ImmediateExecutionQueue(name)
    override fun createConcurrentQueue(name: String, maxConcurrentTasks: Int) = ImmediateExecutionQueue(name)
    override fun isMainQueue() = poseAsMainQueue
}

private class MockPlatformLogger(
    private val logMessages: Boolean,
    private val logStackTraces: Boolean
) : PlatformLogger {
    override fun log(logLevel: LogLevel, message: String) {
        if (logMessages) {
            print(message)
        }
    }

    override fun log(logLevel: LogLevel, throwable: Throwable) {
        if (logStackTraces) {
            throwable.printStackTrace()
        }
    }
}