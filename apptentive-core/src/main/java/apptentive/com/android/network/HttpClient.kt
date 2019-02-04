package apptentive.com.android.network

import apptentive.com.android.async.Promise

interface HttpClient {
    fun send(request: HttpRequest): Promise<HttpResponse>
}