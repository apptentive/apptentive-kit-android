package apptentive.com.android

import apptentive.com.android.core.MainQueueChecker
import apptentive.com.android.core.PlatformLogger
import apptentive.com.android.core.Provider
import apptentive.com.android.util.LogLevel
import org.junit.After
import org.junit.Before

open class TestCase {
    // Before/After

    @Before
    fun setUp() {
        Provider.register(createPlatformLogger())
        Provider.register(createMainQueueChecker())
    }

    @After
    fun tearDown() {
        Provider.clear()
    }

    //endregion

    //region Inheritance

    protected fun createMainQueueChecker(): MainQueueChecker = MockMainQueueChecker(false)
    protected fun createPlatformLogger(): PlatformLogger = MockPlatformLogger()

    //endregion
}

private class MockMainQueueChecker(val poseAsMainQueue: Boolean) : MainQueueChecker {
    override fun isMainQueue(): Boolean = poseAsMainQueue

}

private class MockPlatformLogger : PlatformLogger {
    override fun log(logLevel: LogLevel, message: String) {
        print(message)
    }

    override fun log(logLevel: LogLevel, throwable: Throwable) {
        throwable.printStackTrace()
    }

}