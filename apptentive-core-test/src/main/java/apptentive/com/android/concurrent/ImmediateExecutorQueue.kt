package apptentive.com.android.concurrent

import apptentive.com.android.core.TimeInterval
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Executes tasks synchronously on the same thread as dispatched.
 */
class ImmediateExecutorQueue(
    name: String? = null,
    private val dispatchManually: Boolean = false
) : ExecutorQueue(name ?: "Test Queue") {
    private var currentThread: Thread? = null
    private val tasks = mutableListOf<() -> Unit>()

    override val isCurrent get() = Thread.currentThread() == currentThread

    override fun execute(delay: TimeInterval, task: () -> Unit) {
        if (dispatchManually) {
            tasks.add(task)
        } else {
            dispatchTask(task)
        }
    }

    fun dispatchAll() {
        val temp = tasks.toList()
        tasks.clear()
        for (task in temp) {
            dispatchTask(task)
        }
    }

    private fun dispatchTask(task: () -> Unit) {
        currentThread = Thread.currentThread()
        try {
            task()
        } finally {
            currentThread = null
        }
    }

    override fun stop() {
    }
}

/**
 * Executes tasks asynchronously on the background thread but would block the caller until tasks are done.
 */
class BlockingExecutorQueue(name: String) : ExecutorQueue(name) {
    private val threadGroup: ThreadGroup = ThreadGroup(name)
    private val executor: ExecutorService

    init {
        executor = Executors.newFixedThreadPool(1) { runnable ->
            Thread(threadGroup, runnable)
        }
    }

    override val isCurrent: Boolean get() = Thread.currentThread().threadGroup == threadGroup

    override fun execute(delay: TimeInterval, task: () -> Unit) {
        // there must be a better way
        val mutex = Object()
        val waiting = AtomicBoolean(true)

        executor.execute {
            synchronized(mutex) {
                try {
                    task()
                } finally {
                    waiting.set(false)
                    mutex.notifyAll()
                }
            }
        }

        synchronized(mutex) {
            while (waiting.get()) {
                mutex.wait()
            }
        }
    }

    override fun stop() {
        executor.shutdown()
    }
}