package apptentive.com.android.async

interface Promise<T> {
    fun then(onValue: (value: T) -> Unit, onError: (e: Exception) -> Unit = {}): Promise<T>
    fun catchError(e: Exception): Promise<T>
}