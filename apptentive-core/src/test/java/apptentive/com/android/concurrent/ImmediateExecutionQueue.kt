package apptentive.com.android.concurrent

class ImmediateExecutionQueue(name: String) : ExecutionQueue(name) {
    override val isCurrent get() = true

    override fun dispatch(task: () -> Unit) {
        task()
    }

    override fun stop() {
    }
}