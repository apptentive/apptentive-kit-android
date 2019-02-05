package apptentive.com.android.concurrent

import android.os.Looper

abstract class ExecutionQueue(val name: String) {
    abstract val isCurrent: Boolean

    abstract fun dispatch(task: () -> Unit)
    abstract fun stop()

    companion object {
        val mainQueue: ExecutionQueue = createMainQueue()
        val stateQueue: ExecutionQueue = createStateQueue()

        private fun createMainQueue(): ExecutionQueue {
            return SerialExecutionQueue(Looper.getMainLooper(), "main")
        }

        private fun createStateQueue(): ExecutionQueue {
            return SerialExecutionQueue("apptentive")
        }
    }
}