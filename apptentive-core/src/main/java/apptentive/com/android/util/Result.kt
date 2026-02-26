package apptentive.com.android.util

@InternalUseOnly
sealed class Result<out T : Any> {
    data class Success<out T : Any>(val data: T, val metadata: ResponseMetadata? = null) : Result<T>()
    data class Error(val data: Any, val error: Throwable) : Result<Nothing>()
}
