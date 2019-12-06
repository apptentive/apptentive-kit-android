package apptentive.com.android.concurrent

import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.core.ExecutorFactory
import apptentive.com.android.core.TimeInterval

abstract class ExecutorQueue(val name: String) : Executor {
    abstract val isCurrent: Boolean

    override fun execute(task: () -> Unit) {
        execute(0.0, task)
    }

    abstract fun execute(delay: TimeInterval, task: () -> Unit)
    abstract fun stop()

    companion object {
        private val queueFactory get() = DependencyProvider.of<ExecutorFactory>()
        val mainQueue: ExecutorQueue = queueFactory.createMainQueue()

        fun createSerialQueue(name: String) = queueFactory.createSerialQueue(name)
        fun createConcurrentQueue(name: String, maxConcurrentTasks: Int? = null) =
            queueFactory.createConcurrentQueue(name, maxConcurrentTasks)
    }
}