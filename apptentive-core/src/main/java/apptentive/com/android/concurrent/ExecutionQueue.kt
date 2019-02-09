package apptentive.com.android.concurrent

import apptentive.com.android.core.ExecutionQueueFactory
import apptentive.com.android.core.Provider
import apptentive.com.android.core.TimeInterval
import apptentive.com.android.core.UNDEFINED

abstract class ExecutionQueue(val name: String) {
    abstract val isCurrent: Boolean

    fun dispatch(task: () -> Unit) {
        dispatch(0.0, task)
    }
    abstract fun dispatch(delay: TimeInterval, task: () -> Unit)
    abstract fun stop()

    companion object {
        private val queueFactory get() = Provider.of<ExecutionQueueFactory>()
        val mainQueue: ExecutionQueue = queueFactory.createMainQueue()
        val stateQueue: ExecutionQueue = queueFactory.createSerialQueue("apptentive")
        val isMainQueue = queueFactory.isMainQueue()

        fun createSerialQueue(name: String) = queueFactory.createSerialQueue(name)
        fun createConcurrentQueue(name: String, maxConcurrentTasks: Int = UNDEFINED) =
            queueFactory.createConcurrentQueue(name, maxConcurrentTasks)
    }
}