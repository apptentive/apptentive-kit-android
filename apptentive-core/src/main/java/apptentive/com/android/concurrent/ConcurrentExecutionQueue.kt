package apptentive.com.android.concurrent

import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.core
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

internal class ConcurrentExecutionQueue : ExecutionQueue {
    private val executor: ExecutorService
    private val nextWorkerThreadId = AtomicInteger(0)
    private val threadGroup: ThreadGroup

    constructor(name: String) : this(name, numberOfCores)

    constructor(name: String, maxConcurrentTasks: Int) : super(name) {
        if (maxConcurrentTasks < 1) {
            throw IllegalArgumentException("Invalid number of max concurrent tasks: $maxConcurrentTasks")
        }

        // create a custom thread group so we can identify worker threads
        threadGroup = ThreadGroup(name)

        // create a fixed pool executor and track names for the worker threads
        executor = Executors.newFixedThreadPool(maxConcurrentTasks) { r ->
            Thread(threadGroup, r, "$name (thread-${nextWorkerThreadId.incrementAndGet()})")
        }
    }

    override val isCurrent: Boolean
        get() = Thread.currentThread().threadGroup == threadGroup

    override fun dispatch(task: () -> Unit) {
        executor.execute {
            try {
                task()
            } catch (e: Exception) {
                Log.e(core, e, "Exception while dispatching task")
            }
        }
    }

    override fun stop() {
        executor.shutdownNow()
    }

    companion object {
        private val numberOfCores = Runtime.getRuntime().availableProcessors()
    }
}