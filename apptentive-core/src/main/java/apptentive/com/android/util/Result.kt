package apptentive.com.android.util

@InternalUseOnly
sealed class Result<out T : Any> {
    data class Success<out T : Any>(val data: T) : Result<T>()
    data class Error(val error: Throwable) : Result<Nothing>()
}
