package apptentive.com.android.network

import apptentive.com.android.concurrent.ExecutionQueue
import java.nio.charset.Charset

/**
 * Represents HTTP-request dispatcher
 */
interface HttpClient {
    fun <T> send(request: HttpRequest<T>)
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
     */
    override fun <T> send(request: HttpRequest<T>) {
        networkQueue.dispatch {
            try {
                sendSyncGuarded(request)
            } catch (e: Exception) {
                request.notifyError(e)
            }
        }
    }

    /**
     * Sends HTTP-request synchronously.
     * @param request request to be dispatched.
     */
    private fun <T> sendSyncGuarded(request: HttpRequest<T>) {
        val networkResponse = performRequest(request)

        // response ok?
        if (networkResponse.statusCode in 200..299) {
            val response = HttpResponse(
                networkResponse.statusCode,
                networkResponse.statusMessage,
                request.readResponseObject(networkResponse.content),
                networkResponse.headers,
                networkResponse.duration
            )
            request.notifySuccess(response)
            return
        }

        // attempt to retry
        val retryPolicy = retryPolicyForRequest(request)
        if (retryPolicy.shouldRetry(networkResponse.statusCode, request.numRetries)) {
            networkQueue.dispatch(retryPolicy.getRetryDelay(request.numRetries)) {
                request.numRetries++
                send(request)
            }
        } else {
            val errorMessage = networkResponse.content.toString(Charsets.UTF_8)
            throw UnexpectedResponseException(networkResponse.statusCode, networkResponse.statusMessage, errorMessage)
        }
    }

    /**
     * Sends HTTP-request synchronously.
     *
     * @param request request to be dispatched.
     * @return raw HTTP-response.
     */
    private fun performRequest(request: HttpRequest<*>): HttpNetworkResponse {
        // check network connection
        if (!network.isNetworkConnected) {
            throw NetworkUnavailableException("Network is not available")
        }

        return network.performRequest(request)
    }

    /**
     * @return retry policy for the [request]
     */
    private fun retryPolicyForRequest(request: HttpRequest<*>) = request.retryPolicy ?: this.retryPolicy
}