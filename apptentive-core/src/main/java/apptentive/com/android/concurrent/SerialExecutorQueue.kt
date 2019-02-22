package apptentive.com.android.concurrent

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import apptentive.com.android.core.TimeInterval
import apptentive.com.android.core.toMilliseconds
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.core

class SerialExecutorQueue : ExecutorQueue {
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

    override fun execute(delay: TimeInterval, task: () -> Unit) {
        if (delay > 0) {
            val delayMillis = toMilliseconds(delay).toLong()
            handler.postDelayed({ dispatchSync(task) }, delayMillis)
        } else {
            handler.post {
                dispatchSync(task)
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

    //region Helpers

    private fun dispatchSync(task: () -> Unit) {
        try {
            task()
        } catch (e: Exception) {
            Log.e(core, "Exception while dispatching task", e)
        }
    }

    //endregion
}
