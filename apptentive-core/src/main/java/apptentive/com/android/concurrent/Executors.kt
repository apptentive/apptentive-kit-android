package apptentive.com.android.concurrent

/**
 * Executor container class.
 *
 * @param io executor for dispatching background IO operations
 * @param callback executor for dispatching callback operations
 */
data class Executors(val io: Executor, val callback: Executor)
