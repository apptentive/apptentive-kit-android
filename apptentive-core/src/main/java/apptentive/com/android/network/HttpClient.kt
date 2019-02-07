package apptentive.com.android.network

import apptentive.com.android.concurrent.ExecutionQueue
import apptentive.com.android.concurrent.Promise
import apptentive.com.android.core.TimeInterval

data class HttpClientConfiguration(val networkQueue: ExecutionQueue, val dispatchQueue: ExecutionQueue) {
    var retryPolicy: HttpRetryPolicy = HttpRetryPolicyDefault()
    var connectTimeout = DEFAULT_CONNECTION_TIMEOUT
    var readTimeout = DEFAULT_READ_TIMEOUT

    companion object {
        const val DEFAULT_CONNECTION_TIMEOUT : TimeInterval = 45.0
        const val DEFAULT_READ_TIMEOUT : TimeInterval = 45.0
    }
}

abstract class HttpClient(val configuration: HttpClientConfiguration) {
    abstract fun <T : HttpRequest> send(request: T): Promise<T>
}