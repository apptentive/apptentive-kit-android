package apptentive.com.android.network

import apptentive.com.android.async.Promise

class HttpClientImpl(configuration: HttpClientConfiguration) : HttpClient(configuration) {
    override fun send(request: HttpRequest): Promise<HttpResponse> {
        TODO("send")
    }
}
