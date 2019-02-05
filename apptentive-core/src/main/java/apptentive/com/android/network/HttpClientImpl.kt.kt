package apptentive.com.android.network

import apptentive.com.android.concurrent.Promise

class HttpClientImpl(configuration: HttpClientConfiguration) : HttpClient(configuration) {
    override fun send(request: HttpRequest): Promise<HttpResponse> {
        TODO("send")
    }
}
