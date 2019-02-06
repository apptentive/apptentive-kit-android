package apptentive.com.android.network

import apptentive.com.android.concurrent.ExecutionQueue
import apptentive.com.android.concurrent.Promise
import apptentive.com.android.concurrent.PromiseImpl
import java.lang.Exception
import java.net.URL

class HttpClientImpl(configuration: HttpClientConfiguration) : HttpClient(configuration) {
    /** Contains the list of active (not finished) requests */
    private val activeRequests: MutableList<HttpRequest> = mutableListOf()
    private val networkQueue: ExecutionQueue get() = configuration.networkQueue

    override fun send(request: HttpRequest): Promise<HttpResponse> {
        activeRequests.add(request)
        return dispatchRequest(request)
    }

    private fun dispatchRequest(request: HttpRequest): Promise<HttpResponse> {
        val promise = PromiseImpl<HttpResponse>()
        networkQueue.dispatch {
            try {
                var response = dispatchRequestSync(request)
                promise.onValue(response)
            } catch (e: Exception) {
                promise.onError(e)
            }
        }
        return promise
    }

    private fun dispatchRequestSync(request: HttpRequest): HttpResponse {
        val startTime = System.currentTimeMillis()
        var url = URL(request.url)
        val duration = System.currentTimeMillis() - startTime
        TODO("dispatchRequestSync")
    }
}
