package apptentive.com.android.network

import apptentive.com.android.concurrent.Promise

class HttpClientImpl(configuration: HttpClientConfiguration) : HttpClient(configuration) {
    /** Contains the list of active (not finished) requests */
    private val activeRequests: List<HttpRequest> = mutableListOf()

    override fun send(request: HttpRequest): Promise<HttpResponse> {
        TODO("send")
    }
}
