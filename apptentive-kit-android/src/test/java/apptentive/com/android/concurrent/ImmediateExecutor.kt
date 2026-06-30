package apptentive.com.android.concurrent

import apptentive.com.android.core.concurrent.Executor

object ImmediateExecutor : Executor {
    override fun execute(task: () -> Unit) {
        task()
    }
}
