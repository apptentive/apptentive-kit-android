package apptentive.com.android.concurrent

class PromiseImpl<T> : Promise<T> {
    private var valueCallback: (value: T) -> Unit = {}
    private var errorCallback: (e: Exception) -> Unit = {}

    fun onValue(value: T) {
        valueCallback(value)
    }

    fun onError(e: Exception) {
        errorCallback(e)
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