package apptentive.com.android.network

import apptentive.com.android.concurrent.ExecutionQueue
import apptentive.com.android.concurrent.Promise
import apptentive.com.android.concurrent.PromiseImpl
import java.nio.charset.Charset

/**
 * Class responsible for async HTTP-request dispatching.
 *
 * @param [network] underlying HTTP-network implementation.
 * @param [networkQueue] execution queue used for sync request dispatching.
 * @param [retryPolicy] default retry policy for HTTP-request with no custom policy.
 */
class HttpClient(
    private val network: HttpNetwork,
    private val networkQueue: ExecutionQueue,
    private val retryPolicy: HttpRequestRetryPolicy
) {
    /**
     * Sends HTTP-request asynchronously.
     *
     * @param request request to be dispatched.
     * @return promise which represents an async result for the request.
     */
    fun <T : HttpRequest> send(request: T): Promise<T> {
        val promise = PromiseImpl<T>(request.callbackQueue)
        send(request, promise)
        return promise
    }

    /**
     * Sends HTTP-request asynchronously.
     *
     * @param request request to be dispatched.
     * @param promise which represents an async result for the request.
     */
    private fun <T : HttpRequest> send(request: T, promise: PromiseImpl<T>) {
        networkQueue.dispatch {
            sendSync(request, promise)
        }
    }

    /**
     * Sends HTTP-request synchronously.
     *
     * @param request request to be dispatched.
     * @param promise which represents an async result for the request.
     */
    private fun <T : HttpRequest> sendSync(request: T, promise: PromiseImpl<T>) {
        try {
            if (sendSync(request)) {
                promise.onValue(request)
            } else {
                val retryPolicy = retryPolicyForRequest(request)
                if (retryPolicy.shouldRetry(request)) {
                    networkQueue.dispatch(retryPolicy.getRetryDelay(request)) {
                        request.numRetries++
                        request.reset()
                        sendSync(request, promise)
                    }
                } else {
                    TODO("Fail request")
                }
            }
        } catch (e: Exception) {
            promise.onError(e)
        }
    }

    /**
     * Sends HTTP-request synchronously.
     *
     * @param request request to be dispatched.
     * @return flag indicating if request has completed successfully.
     */
    private fun sendSync(request: HttpRequest): Boolean {
        val response = network.performRequest(request)

        // setup request parameters
        request.statusCode = response.statusCode
        request.statusMessage = response.statusMessage
        request.responseHeaders = response.headers

        // request ok?
        if (response.statusCode in 200..299) {
            request.handleResponseBody(response.content)
            return true
        }

        // error message
        request.errorMessage = response.content.toString(Charset.forName("UTF-8"))
        return false
    }

    /**
     * @return retry policy for the [request]
     */
    private fun <T : HttpRequest> retryPolicyForRequest(request: T) = request.retryPolicy ?: this.retryPolicy
}