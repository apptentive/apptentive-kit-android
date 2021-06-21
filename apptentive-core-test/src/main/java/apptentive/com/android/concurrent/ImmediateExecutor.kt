package apptentive.com.android.concurrent

object ImmediateExecutor : Executor {
    override fun execute(task: () -> Unit) {
        task()
    }
}
