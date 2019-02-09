package apptentive.com.android.concurrent

/**
 * Represents the eventual completion (or failure) of an asynchronous operation, and its resulting value.
 */
interface Promise<T> {
    /**
     * Appends fulfillment handlers to the promise, and returns a new promise resolving to the return value
     * of the called handler, or to its original settled value if the promise was not handled.
     */
    fun then(onValue: (value: T) -> Unit): Promise<T>

    /**
     * Appends a rejection handler callback to the promise, and returns a new promise resolving to the return value
     * of the callback if it is called, or to its original fulfillment value if the promise is instead fulfilled.
     */
    fun catch(onError: (e: Exception) -> Unit = {}): Promise<T>
}