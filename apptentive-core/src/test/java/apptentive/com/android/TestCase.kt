package apptentive.com.android

import apptentive.com.android.concurrent.ImmediateExecutionQueue
import apptentive.com.android.core.ExecutionQueueFactory
import apptentive.com.android.core.PlatformLogger
import apptentive.com.android.core.Provider
import apptentive.com.android.util.LogLevel
import org.junit.After
import org.junit.Assert.*
import org.junit.Before

open class TestCase {
    val results = mutableListOf<Any>()

    // Before/After

    @Before
    fun setUp() {
        Provider.register(createPlatformLogger())
        Provider.register(createExecutionQueueFactory())
        results.clear()
    }

    @After
    fun tearDown() {
        Provider.clear()
    }

    //endregion

    //region Inheritance

    protected fun createExecutionQueueFactory(): ExecutionQueueFactory = MockExecutionQueueFactory(false)
    protected fun createPlatformLogger(): PlatformLogger = MockPlatformLogger()

    //endregion

    //region Results

    protected fun addResult(result: Any) {
        results.add(result)
    }

    protected fun assertResults(vararg expected: Any, clearResults: Boolean = true) {
        assertEquals(expected, results)
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

private class MockPlatformLogger : PlatformLogger {
    override fun log(logLevel: LogLevel, message: String) {
        print(message)
    }

    override fun log(logLevel: LogLevel, throwable: Throwable) {
        throwable.printStackTrace()
    }
}