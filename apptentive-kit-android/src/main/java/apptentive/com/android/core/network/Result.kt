package apptentive.com.android.core.network

internal sealed class Result<out T : Any> {
    data class Success<out T : Any>(val data: T, val metadata: ResponseMetadata? = null) : Result<T>()
    data class Error(val data: Any, val error: Throwable) : Result<Nothing>()
}
