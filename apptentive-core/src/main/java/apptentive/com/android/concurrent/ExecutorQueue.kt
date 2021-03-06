package apptentive.com.android.concurrent

import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.core.ExecutorFactory
import apptentive.com.android.core.TimeInterval
import apptentive.com.android.util.InternalUseOnly

@InternalUseOnly
abstract class ExecutorQueue(private val name: String) : Executor {
    abstract val isCurrent: Boolean

    override fun execute(task: () -> Unit) {
        execute(0.0, task)
    }

    abstract fun execute(delay: TimeInterval, task: () -> Unit)
    abstract fun stop()

    companion object {
        private val executorFactory get() = DependencyProvider.of<ExecutorFactory>()
        val mainQueue: ExecutorQueue = executorFactory.createMainQueue()

        fun createSerialQueue(name: String) = executorFactory.createSerialQueue(name)
        fun createConcurrentQueue(name: String, maxConcurrentTasks: Int? = null) =
            executorFactory.createConcurrentQueue(name, maxConcurrentTasks)
    }
}
