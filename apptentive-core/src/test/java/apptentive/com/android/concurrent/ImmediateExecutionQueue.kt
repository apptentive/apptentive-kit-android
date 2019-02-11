package apptentive.com.android.concurrent

import apptentive.com.android.core.TimeInterval
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Executes tasks synchronously on the same thread as dispatched.
 */
class ImmediateExecutionQueue(name: String) : ExecutionQueue(name) {
    private var currentThread: Thread? = null

    override val isCurrent get() = Thread.currentThread() == currentThread

    override fun dispatch(delay: TimeInterval, task: () -> Unit) {
        currentThread = Thread.currentThread()
        task()
        currentThread = null
    }

    override fun stop() {
    }
}

/**
 * Executes tasks asynchronously on the background thread but would block the caller until tasks are done.
 */
class BlockingExecutionQueue(name: String) : ExecutionQueue(name) {
    private val threadGroup: ThreadGroup = ThreadGroup(name)
    private val executor: ExecutorService

    init {
        executor = Executors.newFixedThreadPool(1) { runnable ->
            Thread(threadGroup, runnable)
        }
    }

    override val isCurrent: Boolean get() = Thread.currentThread().threadGroup == threadGroup

    override fun dispatch(delay: TimeInterval, task: () -> Unit) {
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