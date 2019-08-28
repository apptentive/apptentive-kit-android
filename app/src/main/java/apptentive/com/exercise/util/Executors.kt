package apptentive.com.exercise.util

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class AppExecutors(val io: Executor, val ui: Executor) {
    private constructor() : this(Executors.newSingleThreadExecutor(), MainThreadExecutor())

    companion object {
        val defaultExecutors = AppExecutors()
    }
}

private class MainThreadExecutor : Executor {
    private val handler = Handler(Looper.getMainLooper())

    override fun execute(command: Runnable) {
        handler.post(command)
    }
}