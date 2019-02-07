package apptentive.com.android.core

import android.os.Looper
import apptentive.com.android.concurrent.ConcurrentExecutionQueue
import apptentive.com.android.concurrent.ExecutionQueue
import apptentive.com.android.concurrent.SerialExecutionQueue

/**
 */
interface ExecutionQueueFactory : Providable {
    fun createMainQueue(): ExecutionQueue
    fun createSerialQueue(name: String): ExecutionQueue
    fun createConcurrentQueue(name: String, maxConcurrentTasks: Int): ExecutionQueue
    fun isMainQueue() : Boolean
}

fun createExecutionQueueFactory(): ExecutionQueueFactory = ExecutionQueueFactoryImpl()

private class ExecutionQueueFactoryImpl : ExecutionQueueFactory {
    override fun createMainQueue(): ExecutionQueue {
        return SerialExecutionQueue(Looper.getMainLooper(), "main")
    }

    override fun createSerialQueue(name: String): ExecutionQueue {
        return SerialExecutionQueue(name)
    }

    override fun createConcurrentQueue(name: String, maxConcurrentTasks: Int): ExecutionQueue {
        if (maxConcurrentTasks == UNDEFINED) {
            return ConcurrentExecutionQueue(name) // let the implementation decide
        }
        return ConcurrentExecutionQueue(name, maxConcurrentTasks)
    }

    override fun isMainQueue(): Boolean {
        return Looper.getMainLooper() == Looper.myLooper()
    }
}