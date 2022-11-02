package apptentive.com.android.feedback.message

import apptentive.com.android.concurrent.Executor
import apptentive.com.android.concurrent.SerialExecutorQueue
import apptentive.com.android.core.TimeInterval
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.MESSAGE_CENTER

internal typealias PollingTask = () -> Unit

internal class MessagePollingScheduler(private val executor: Executor) : PollingScheduler {

    private var pollingTask: PollingTask? = null
    private var pollingInterval: TimeInterval = 300.0

    override fun startPolling(
        delay: TimeInterval /* = kotlin.Double */,
        resetInterval: Boolean,
        task: PollingTask
    ) {
        if (resetInterval) stopPolling()
        pollingTask = task
        pollingInterval = delay
        dispatchTask()
        Log.d(MESSAGE_CENTER, "Start polling messages")
    }

    override fun onFetchFinish() {
        dispatchTask()
    }

    override fun stopPolling() {
        pollingTask = null
        Log.d(MESSAGE_CENTER, "Stop polling messages")
    }

    private fun dispatchTask() {
        Log.d(MESSAGE_CENTER, "Dispatching next message center task")
        if (executor is SerialExecutorQueue) {
            executor.execute(pollingInterval) {
                pollingTask?.invoke()
            }
        }
    }

    override fun isPolling() = pollingTask != null
}
