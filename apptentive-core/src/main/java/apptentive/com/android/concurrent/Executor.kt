package apptentive.com.android.concurrent

/**
 * An object that executes submitted runnable tasks. This
 * interface provides a way of decoupling task submission from the
 * mechanics of how each task will be run, including details of thread
 * use, scheduling, etc.
 */
interface Executor {
    fun execute(task: () -> Unit)
}