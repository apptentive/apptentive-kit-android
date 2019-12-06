package apptentive.com.android.core

import android.os.Looper
import apptentive.com.android.concurrent.ConcurrentExecutorQueue
import apptentive.com.android.concurrent.ExecutorQueue
import apptentive.com.android.concurrent.SerialExecutorQueue

interface ExecutorFactory {
    fun createMainQueue(): ExecutorQueue
    fun createSerialQueue(name: String): ExecutorQueue
    fun createConcurrentQueue(name: String, maxConcurrentTasks: Int? = null): ExecutorQueue
}

class AndroidExecutorFactoryProvider : Provider<ExecutorFactory> {
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