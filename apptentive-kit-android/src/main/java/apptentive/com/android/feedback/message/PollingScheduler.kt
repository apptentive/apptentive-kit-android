package apptentive.com.android.feedback.message

import apptentive.com.android.core.TimeInterval
import apptentive.com.android.core.util.InternalUseOnly

@InternalUseOnly
interface PollingScheduler {
    fun startPolling(delay: TimeInterval, resetInterval: Boolean = false, task: PollingTask)
    fun stopPolling()
    fun isPolling(): Boolean
    fun onFetchFinish()
}
