package apptentive.com.android.network

import apptentive.com.android.concurrent.ExecutionQueue
import apptentive.com.android.concurrent.Promise
import apptentive.com.android.concurrent.AsyncPromise
import apptentive.com.android.core.UNDEFINED
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags

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
 * @param [listener] optional [HttpClientListener]
 */
class HttpClientImpl(
    private val network: HttpNetwork,
    private val networkQueue: ExecutionQueue,
    private val retryPolicy: HttpRequestRetryPolicy,
    private val listener: HttpClientListener? = null
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
            // notify listener
            notifyOnStart(request)

            // send sync
            sendSync(request, promise)
        }
        return promise
    }

    private fun <T> sendSync(
        request: HttpRequest<T>,
        promise: AsyncPromise<HttpResponse<T>>
    ) {
        try {
            // send sync request
            val response = getResponse(request, promise)

            // fulfill promise
            if (response != null) {
                promise.onValue(response)
            }
        } catch (e: Exception) {
            // notify listener
            notifyOnComplete(request)

            // reject promise
            promise.onError(e)
        }
    }

    /**
     * Sends HTTP-request synchronously.
     * @param request request to be dispatched.
     * @return [HttpResponse] if request completed successfully or null if failed or retrying
     */
    private fun <T> getResponse(
        request: HttpRequest<T>,
        promise: AsyncPromise<HttpResponse<T>>
    ): HttpResponse<T>? {
        // check network connection first
        if (!network.isNetworkConnected) {
            // attempt to retry
            if (retry(request, UNDEFINED, promise)) {
                return null
            }

            throw NetworkUnavailableException("Network is not available")
        }

        // raw response
        val rawResponse = network.performRequest(request)

        // status
        val statusCode = rawResponse.statusCode
        val statusMessage = rawResponse.statusMessage

        // successful?
        if (statusCode in 200..299) {
            return HttpResponse(
                statusCode,
                statusMessage,
                request.createResponseObject(rawResponse.content),
                rawResponse.headers,
                rawResponse.duration
            )
        }

        // attempt to retry
        if (!retry(request, statusCode, promise)) {
            val errorMessage = rawResponse.content.toString(Charsets.UTF_8)
            throw UnexpectedResponseException(statusCode, statusMessage, errorMessage)
        }

        return null
    }

    private fun <T >retry(
        request: HttpRequest<T>,
        statusCode: Int,
        promise: AsyncPromise<HttpResponse<T>>
    ): Boolean {
        val retryPolicy = retryPolicyForRequest(request)
        if (retryPolicy.shouldRetry(statusCode, request.numRetries)) {
            val delay = retryPolicy.getRetryDelay(request.numRetries)
            networkQueue.dispatch(delay) {
                request.numRetries++

                // notify listener
                notifyOnRetry(request)

                // send request
                sendSync(request, promise)
            }
            return true
        }
        return false
    }

    //region Listener notifications

    private fun <T> notifyOnStart(request: HttpRequest<T>) {
        try {
            listener?.onRequestStart(this, request)
        } catch (e: Exception) {
            Log.e(LogTags.network, "Exception while notifying request start listener", e)
        }
    }

    private fun <T> notifyOnRetry(request: HttpRequest<T>) {
        try {
            listener?.onRequestRetry(this, request)
        } catch (e: Exception) {
            Log.e(LogTags.network, "Exception while notifying request retry listener", e)
        }
    }

    private fun <T> notifyOnComplete(request: HttpRequest<T>) {
        try {
            listener?.onRequestComplete(this, request)
        } catch (e: Exception) {
            Log.e(LogTags.network, "Exception while notifying request complete listener", e)
        }
    }

    //endregion

    //region Helpers

    /**
     * @return retry policy for the [request]
     */
    private fun retryPolicyForRequest(request: HttpRequest<*>) = request.retryPolicy ?: this.retryPolicy

    //endregion
}

interface HttpClientListener {
    fun onRequestStart(client: HttpClient, request: HttpRequest<*>)
    fun onRequestRetry(client: HttpClient, request: HttpRequest<*>)
    fun onRequestComplete(client: HttpClient, request: HttpRequest<*>)
}