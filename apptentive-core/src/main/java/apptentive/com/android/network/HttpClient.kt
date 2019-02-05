package apptentive.com.android.network

import apptentive.com.android.concurrent.Promise
import apptentive.com.android.core.TimeInterval

data class HttpClientConfiguration(val timeout: TimeInterval) {
    var retryPolicy: HttpRetryPolicy = HttpRetryPolicyDefault()
}

abstract class HttpClient(val configuration: HttpClientConfiguration) {
    abstract fun send(request: HttpRequest): Promise<HttpResponse>
}