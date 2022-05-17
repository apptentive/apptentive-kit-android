package apptentive.com.android.feedback.message

import apptentive.com.android.core.TimeInterval

internal interface PollingScheduler {
    fun startPolling(delay: TimeInterval, resetInterval: Boolean = false, task: PollingTask)
    fun stopPolling()
    fun onFetchFinish()
}
