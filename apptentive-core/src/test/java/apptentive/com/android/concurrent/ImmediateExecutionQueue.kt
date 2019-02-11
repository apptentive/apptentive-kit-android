package apptentive.com.android.concurrent

import apptentive.com.android.core.TimeInterval

class ImmediateExecutionQueue(name: String) : ExecutionQueue(name) {
    override val isCurrent get() = true

    override fun dispatch(delay: TimeInterval, task: () -> Unit) {
        task()
    }

    override fun stop() {
    }
}