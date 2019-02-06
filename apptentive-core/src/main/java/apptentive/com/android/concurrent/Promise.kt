package apptentive.com.android.concurrent

interface Promise<T> {
    fun then(onValue: (value: T) -> Unit): Promise<T>
    fun catchError(onError: (e: Exception) -> Unit = {}): Promise<T>
}