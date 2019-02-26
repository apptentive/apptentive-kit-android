package apptentive.com.android.concurrent

import apptentive.com.android.core.ExecutorQueueFactory
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.core.TimeInterval
import apptentive.com.android.core.UNDEFINED

abstract class ExecutorQueue(val name: String) : Executor {
    abstract val isCurrent: Boolean

    override fun execute(task: () -> Unit) {
        execute(0.0, task)
    }

    abstract fun execute(delay: TimeInterval, task: () -> Unit)
    abstract fun stop()

    companion object {
        private val queueFactory get() = DependencyProvider.of<ExecutorQueueFactory>()
        val mainQueue: ExecutorQueue = queueFactory.createMainQueue()
        val stateQueue: ExecutorQueue = queueFactory.createSerialQueue("apptentive")
        val isMainQueue = queueFactory.isMainQueue()

        fun createSerialQueue(name: String) = queueFactory.createSerialQueue(name)
        fun createConcurrentQueue(name: String, maxConcurrentTasks: Int = UNDEFINED) =
            queueFactory.createConcurrentQueue(name, maxConcurrentTasks)
    }
}