package apptentive.com.android.core.concurrent

import android.os.Looper
import apptentive.com.android.core.Provider

internal interface ExecutorFactory {
    fun createMainQueue(): ExecutorQueue
    fun createSerialQueue(name: String): ExecutorQueue
    fun createConcurrentQueue(name: String, maxConcurrentTasks: Int? = null): ExecutorQueue
}

internal class AndroidExecutorFactoryProvider :
    Provider<ExecutorFactory> {
    private val factory: ExecutorFactory by lazy { AndroidExecutorFactory() }
    override fun get(): ExecutorFactory = factory
}

private class AndroidExecutorFactory : ExecutorFactory {
    override fun createMainQueue(): ExecutorQueue {
        return SerialExecutorQueue(Looper.getMainLooper(), "main")
    }

    override fun createSerialQueue(name: String): ExecutorQueue {
        return SerialExecutorQueue(name)
    }

    override fun createConcurrentQueue(name: String, maxConcurrentTasks: Int?): ExecutorQueue {
        return ConcurrentExecutorQueue(name, maxConcurrentTasks)
    }
}
