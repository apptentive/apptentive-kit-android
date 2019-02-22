package apptentive.com.android.core

import android.os.Looper
import apptentive.com.android.concurrent.ConcurrentExecutorQueue
import apptentive.com.android.concurrent.ExecutorQueue
import apptentive.com.android.concurrent.SerialExecutorQueue

/**
 */
interface ExecutorQueueFactory : Providable {
    fun createMainQueue(): ExecutorQueue
    fun createSerialQueue(name: String): ExecutorQueue
    fun createConcurrentQueue(name: String, maxConcurrentTasks: Int): ExecutorQueue
    fun isMainQueue() : Boolean
}

fun createExecutionQueueFactory(): ExecutorQueueFactory = ExecutorQueueFactoryImpl()

private class ExecutorQueueFactoryImpl : ExecutorQueueFactory {
    override fun createMainQueue(): ExecutorQueue {
        return SerialExecutorQueue(Looper.getMainLooper(), "main")
    }

    override fun createSerialQueue(name: String): ExecutorQueue {
        return SerialExecutorQueue(name)
    }

    override fun createConcurrentQueue(name: String, maxConcurrentTasks: Int): ExecutorQueue {
        if (maxConcurrentTasks == UNDEFINED) {
            return ConcurrentExecutorQueue(name) // let the implementation decide
        }
        return ConcurrentExecutorQueue(name, maxConcurrentTasks)
    }

    override fun isMainQueue(): Boolean {
        return Looper.getMainLooper() == Looper.myLooper()
    }
}