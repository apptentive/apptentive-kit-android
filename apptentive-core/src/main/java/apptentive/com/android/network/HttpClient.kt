package apptentive.com.android.network

import apptentive.com.android.concurrent.ExecutionQueue
import apptentive.com.android.concurrent.Promise
import apptentive.com.android.concurrent.PromiseImpl
import java.nio.charset.Charset

class HttpClient(
    private val network: HttpNetwork,
    private val networkQueue: ExecutionQueue,
    private val retryPolicy: HttpRequestRetryPolicy
) {
    /**
     * Sends HTTP-request asynchronously.
     *
     * @return promise for the request network operation.
     */
    fun <T : HttpRequest> send(request: T): Promise<T> {
        val promise = PromiseImpl<T>(request.callbackQueue)
        send(request, promise)
        return promise
    }

    private fun <T : HttpRequest> send(request: T, promise: PromiseImpl<T>) {
        networkQueue.dispatch {
            sendSync(request, promise)
        }
    }

    private fun <T : HttpRequest> sendSync(request: T, promise: PromiseImpl<T>) {
        try {
            if (sendSync(request)) {
                promise.onValue(request)
            } else {
                val retryPolicy = retryPolicyForRequest(request)
                if (retryPolicy.shouldRetry(request)) {
                    networkQueue.dispatch(retryPolicy.getRetryDelay(request)) {
                        request.numRetries++
                        sendSync(request, promise)
                    }
                }
            }
        } catch (e: Exception) {
            promise.onError(e)
        }
    }

    /**
     * Performs [request] synchronously and return boolean flag indicating if request
     * was completed successfully.
     */
    private fun sendSync(request: HttpRequest): Boolean {
        val response = network.performRequest(request)

        // setup request parameters
        request.statusCode = response.statusCode
        request.statusMessage = response.statusMessage
        request.responseHeaders = response.headers

        if (response.statusCode in 200..299) {
            request.handleResponseBody(response.content)
            return true
        }

        // error message
        request.errorMessage = response.content.toString(Charset.forName("UTF-8"))
        return false
    }

    private fun <T : HttpRequest> retryPolicyForRequest(request: T) = request.retryPolicy ?: this.retryPolicy
}