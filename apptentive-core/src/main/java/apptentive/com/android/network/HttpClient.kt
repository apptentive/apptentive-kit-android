package apptentive.com.android.network

import apptentive.com.android.concurrent.ExecutionQueue
import apptentive.com.android.concurrent.Promise
import apptentive.com.android.concurrent.AsyncPromise

/**
 * Represents async HTTP-request dispatcher.
 */
interface HttpClient {
    /**
     * Sends HTTP-request asynchronously.
     *
     * @param request request to be sent.
     * @return promise to be fulfilled when request is completed
     */
    fun <T> send(request: HttpRequest<T>): Promise<HttpResponse<T>>
}

/**
 * Class responsible for async HTTP-request dispatching.
 *
 * @param [network] underlying HTTP-network implementation.
 * @param [networkQueue] execution queue used for sync request dispatching.
 * @param [retryPolicy] default retry policy for HTTP-request with no custom policy.
 */
class HttpClientImpl(
    private val network: HttpNetwork,
    private val networkQueue: ExecutionQueue,
    private val retryPolicy: HttpRequestRetryPolicy
) : HttpClient {
    /**
     * Sends HTTP-request asynchronously.
     *
     * @param request request to be sent.
     * @return promise to be fulfilled when request is completed
     */
    override fun <T> send(request: HttpRequest<T>): Promise<HttpResponse<T>> {
        /* promise will be fulfilled on the request's callback queue (or then network queue if missing) */
        val promise = AsyncPromise<HttpResponse<T>>(request.callbackQueue)
        networkQueue.dispatch {
            try {
                send(request, promise)
            } catch (e: Exception) {
                promise.onError(e)
            }
        }
        return promise
    }

    /**
     * Sends HTTP-request synchronously.
     * @param request request to be dispatched.
     * @param promise promise to be fulfilled when request is complete.
     */
    private fun <T> send(
        request: HttpRequest<T>,
        promise: AsyncPromise<HttpResponse<T>>
    ) {
        // check network connection first
        if (!network.isNetworkConnected) {
            throw NetworkUnavailableException("Network is not available")
        }

        // get raw response
        val rawResponse = network.performRequest(request)

        // successful?
        if (rawResponse.statusCode in 200..299) {
            val response = HttpResponse(
                rawResponse.statusCode,
                rawResponse.statusMessage,
                request.createResponseObject(rawResponse.content),
                rawResponse.headers,
                rawResponse.duration
            )
            promise.onValue(response)
            return
        }

        // attempt to retry
        val retryPolicy = retryPolicyForRequest(request)
        if (retryPolicy.shouldRetry(rawResponse.statusCode, request.numRetries)) {
            networkQueue.dispatch(retryPolicy.getRetryDelay(request.numRetries)) {
                request.numRetries++
                send(request)
            }
        } else {
            val errorMessage = rawResponse.content.toString(Charsets.UTF_8)
            throw UnexpectedResponseException(rawResponse.statusCode, rawResponse.statusMessage, errorMessage)
        }
    }

    /**
     * @return retry policy for the [request]
     */
    private fun retryPolicyForRequest(request: HttpRequest<*>) = request.retryPolicy ?: this.retryPolicy
}