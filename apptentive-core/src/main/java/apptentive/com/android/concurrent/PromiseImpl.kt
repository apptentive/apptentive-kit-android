package apptentive.com.android.concurrent

class PromiseImpl<T>(private val dispatchQueue: ExecutionQueue) : Promise<T> {
    private var valueCallback: (value: T) -> Unit = {}
    private var errorCallback: (e: Exception) -> Unit = {}

    fun onValue(value: T) {
        dispatchQueue.dispatch {
            try {
                valueCallback(value)
            } catch (e: Exception) {
                errorCallback(e)
            }
        }
    }

    fun onError(e: Exception) {
        dispatchQueue.dispatch {
            errorCallback(e)
        }
    }

    override fun then(onValue: (value: T) -> Unit): Promise<T> {
        valueCallback = onValue
        return this
    }

    override fun catchError(onError: (e: Exception) -> Unit): Promise<T> {
        errorCallback = onError
        return this
    }
}