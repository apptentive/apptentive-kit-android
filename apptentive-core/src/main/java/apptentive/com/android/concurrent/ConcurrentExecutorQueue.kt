package apptentive.com.android.concurrent

import apptentive.com.android.core.TimeInterval
import apptentive.com.android.core.toMilliseconds
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.core
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

internal class ConcurrentExecutorQueue(name: String, maxConcurrentTasks: Int? = null) :
    ExecutorQueue(name) {
    private val executor: ScheduledExecutorService
    private val nextWorkerThreadId = AtomicInteger(0)
    private val threadGroup: ThreadGroup

    init {
        val maxTasks = maxConcurrentTasks ?: numberOfCores
        require(maxTasks >= 1) { "Invalid number of max concurrent tasks: $maxTasks" }

        // create a custom thread group so we can identify worker threads
        threadGroup = ThreadGroup(name)

        // create a fixed pool executor and track names for the worker threads
        executor = ScheduledThreadPoolExecutor(maxTasks) { r ->
            Thread(threadGroup, r, "$name (thread-${nextWorkerThreadId.incrementAndGet()})")
        }.apply { maximumPoolSize = maxTasks }
    }

    override val isCurrent: Boolean
        get() = Thread.currentThread().threadGroup == threadGroup

    override fun execute(delay: TimeInterval, task: () -> Unit) {
        if (delay > 0) {
            val delayMillis = toMilliseconds(delay).toLong()
            executor.schedule({ dispatchSync(task) }, delayMillis, TimeUnit.MILLISECONDS)
        } else {
            executor.execute {
                dispatchSync(task)
            }
        }
    }

    override fun stop() {
        executor.shutdownNow()
    }

    //region Helpers

    private fun dispatchSync(task: () -> Unit) {
        try {
            task()
        } catch (e: Exception) {
            Log.e(core, "Exception while dispatching task", e)
        }
    }

    //endregion

    companion object {
        private val numberOfCores = Runtime.getRuntime().availableProcessors()
    }
}