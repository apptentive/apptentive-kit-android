package apptentive.com.android.network

import apptentive.com.android.concurrent.ExecutionQueue
import apptentive.com.android.concurrent.Promise
import apptentive.com.android.core.TimeInterval

data class HttpClientConfiguration(val networkQueue: ExecutionQueue) {
    var retryPolicy: HttpRetryPolicy = HttpRetryPolicyDefault()
    var timeout: TimeInterval = 45.0
}

abstract class HttpClient(val configuration: HttpClientConfiguration) {
    abstract fun send(request: HttpRequest): Promise<HttpResponse>
}