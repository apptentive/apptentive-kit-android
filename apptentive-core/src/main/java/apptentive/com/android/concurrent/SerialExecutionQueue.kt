package apptentive.com.android.concurrent

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.core

class SerialExecutionQueue : ExecutionQueue {
    private val handler: Handler
    private val handlerThread: HandlerThread?

    override val isCurrent: Boolean
        get() = Looper.myLooper() == handler.looper

    constructor(name: String) : super(name) {
        val thread = HandlerThread(name)
        thread.start()
        handler = Handler(thread.looper)
        handlerThread = thread
    }

    internal constructor(looper: Looper, name: String) : super(name) {
        handler = Handler(looper)
        handlerThread = null
    }

    override fun dispatch(task: () -> Unit) {
        handler.post {
            try {
                task()
            } catch (e: Exception) {
                Log.e(core, "Exception while dispatching task", e)
            }
        }
    }

    override fun stop() {
        // could use let here
        if (handlerThread != null) {
            handler.removeCallbacks(null)
            handlerThread.quit()
        }
    }
}
