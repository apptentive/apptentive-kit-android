package apptentive.com.android.concurrent

import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags

/**
 * Async implementation of the [Promise] interface
 *
 * @param [callbackExecutor] optional executor to invoke async fulfilment/rejection callbacks.
 */
class AsyncPromise<T>(private val callbackExecutor: Executor? = null) : Promise<T> {
    private var resolveCallback: (value: T) -> Unit = {}
    private var rejectCallback: (e: Exception) -> Unit = {}

    override fun then(onValue: (value: T) -> Unit): Promise<T> {
        resolveCallback = onValue
        return this
    }

    override fun catch(onError: (e: Exception) -> Unit): Promise<T> {
        rejectCallback = onError
        return this
    }

    fun resolve(value: T) {
        if (callbackExecutor != null) {
            callbackExecutor.execute {
                resolveSync(value)
            }
        } else {
            resolveSync(value)
        }
    }

    fun reject(e: Exception) {
        if (callbackExecutor != null) {
            callbackExecutor.execute {
                rejectSync(e)
            }
        } else {
            rejectSync(e)
        }
    }

    private fun resolveSync(value: T) {
        try {
            resolveCallback(value)
        } catch (e: Exception) {
            rejectSync(e)
        }
    }

    private fun rejectSync(e: Exception) {
        try {
            rejectCallback(e)
        } catch (e: Exception) {
            Log.e(LogTags.core,"Exception while rejecting promise", e)
        }
    }
}