package apptentive.com.android.network

import apptentive.com.android.concurrent.ExecutorQueue
import apptentive.com.android.concurrent.Promise
import apptentive.com.android.concurrent.AsyncPromise
import apptentive.com.android.core.UNDEFINED
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags

/**
 * Represents an abstract async HTTP-request dispatcher.
 */
interface HttpClient {
    /**
     * Sends HTTP-request asynchronously.
     *
     * @param request request to be sent.
     * @param onValue promise fulfilment handler
     * @param onError promise rejection handler
     * @return promise to be fulfilled when request is completed
     */
    fun <T> send(
        request: HttpRequest<T>,
        onValue: ((value: HttpResponse<T>) -> Unit)? = null,
        onError: ((e: Exception) -> Unit)? = null
    ): Promise<HttpResponse<T>>
}

/**
 * Concrete implementation of the async HTTP-request dispatcher.
 *
 * @param [network] underlying HTTP-network implementation.
 * @param [networkQueue] execution queue used for sync request dispatching.
 * @param [retryPolicy] default retry policy for HTTP-request with no custom policy.
 * @param [listener] optional [HttpClientListener] for tracking status of the requests.
 */
class DefaultHttpClient(
    private val network: HttpNetwork,
    private val networkQueue: ExecutorQueue,
    private val retryPolicy: HttpRequestRetryPolicy,
    private val listener: HttpClientListener? = null
) : HttpClient {
    //region Request sending

    /**
     * Sends HTTP-request asynchronously.
     *
     * @param request request to be sent.
     * @return promise for the [HttpResponse] to fulfill or reject when completed.
     */
    override fun <T> send(
        request: HttpRequest<T>,
        onValue: ((value: HttpResponse<T>) -> Unit)?,
        onError: ((e: Exception) -> Unit)?
    ): Promise<HttpResponse<T>> {
        /* promise will be fulfilled on the request's callback queue (or then network queue if missing) */
        val promise = AsyncPromise<HttpResponse<T>>(request.callbackExecutor)
        if (onValue != null) {
            promise.then(onValue)
        }
        if (onError != null) {
            promise.catch(onError)
        }
        networkQueue.execute {
            // notify listener
            notifyOnStart(request)

            // send sync
            sendSync(request, promise)
        }
        return promise
    }

    /**
     * Sends HTTP-request synchronously.
     *
     * @param request request to be sent.
     * @param promise for the [HttpResponse] to fulfill or reject when completed.
     */
    private fun <T> sendSync(
        request: HttpRequest<T>,
        promise: AsyncPromise<HttpResponse<T>>
    ) {
        try {
            val response = getResponseSync(request)
            if (response != null) {
                // notify listener
                notifyOnComplete(request)

                // fulfill promise
                promise.resolve(response)
            } else {
                // attempt another retry
                scheduleRetry(request, promise)
            }
        } catch (e: Exception) {
            // notify listener
            notifyOnComplete(request)

            // reject promise
            promise.reject(e)
        }
    }

    /**
     * Receives [HttpResponse] synchronously.
     *
     * @param request request to send.
     * @return [HttpResponse] object if request completed successfully or null if retrying
     */
    private fun <T> getResponseSync(request: HttpRequest<T>): HttpResponse<T>? {
        // check network status
        if (!network.isNetworkConnected) {
            // should we retry?
            if (shouldRetry(request)) {
                return null
            }

            throw NetworkUnavailableException("Network is not available")
        }

        // response body
        val networkResponse = network.performRequest(request)
        networkResponse.use { response ->
            // status
            val statusCode = response.statusCode
            val statusMessage = response.statusMessage

            // successful?
            if (statusCode in 200..299) {
                return HttpResponse(
                    statusCode,
                    statusMessage,
                    request.readResponseObject(response.stream),
                    response.headers,
                    response.duration
                )
            }

            // should we retry?
            if (shouldRetry(request, statusCode)) {
                return null
            }

            // give up
            val errorMessage = response.stream.readBytes().toString(Charsets.UTF_8)
            throw UnexpectedResponseException(statusCode, statusMessage, errorMessage)
        }
    }

    //endregion

    //region Retry

    /**
     * @param statusCode HTTP-status code of the response or null for an exception.
     * @return true if [request] should be retried again
     */
    private fun <T> shouldRetry(request: HttpRequest<T>, statusCode: Int? = null): Boolean {
        val retryPolicy = retryPolicyForRequest(request)
        return retryPolicy.shouldRetry(statusCode ?: UNDEFINED, request.numRetries)
    }

    /**
     * Schedules retry on the network queue according to retry policy.
     *
     * @param request request to be sent.
     * @param promise for the [HttpResponse] to fulfill or reject when completed.
     */
    private fun <T> scheduleRetry(
        request: HttpRequest<T>,
        promise: AsyncPromise<HttpResponse<T>>
    ) {
        val retryPolicy = retryPolicyForRequest(request)
        val delay = retryPolicy.getRetryDelay(request.numRetries)
        networkQueue.execute(delay) {
            request.numRetries++

            // notify listener
            notifyOnRetry(request)

            // send request
            sendSync(request, promise)
        }
    }

    //endregion

    //region Listener notifications

    /** Notify onStart listener */
    private fun <T> notifyOnStart(request: HttpRequest<T>) {
        try {
            listener?.onRequestStart(this, request)
        } catch (e: Exception) {
            Log.e(LogTags.network, "Exception while notifying request start listener", e)
        }
    }

    /** Notify onRetry listener */
    private fun <T> notifyOnRetry(request: HttpRequest<T>) {
        try {
            listener?.onRequestRetry(this, request)
        } catch (e: Exception) {
            Log.e(LogTags.network, "Exception while notifying request retry listener", e)
        }
    }

    /** Notify onComplete listener */
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

/**
 * A callback interface for request lifecycle events.
 */
interface HttpClientListener {
    /**
     * Invoked after request was enqueued.
     */
    fun onRequestStart(client: HttpClient, request: HttpRequest<*>)

    /**
     * Invoked after request was retried.
     */
    fun onRequestRetry(client: HttpClient, request: HttpRequest<*>)

    /**
     * Invoked after request was completed (succeed or failed).
     */
    fun onRequestComplete(client: HttpClient, request: HttpRequest<*>)
}