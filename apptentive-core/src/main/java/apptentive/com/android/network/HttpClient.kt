package apptentive.com.android.network

import apptentive.com.android.concurrent.Executor
import apptentive.com.android.concurrent.ExecutorQueue
import apptentive.com.android.core.UNDEFINED
import apptentive.com.android.util.InternalUseOnly
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags
import apptentive.com.android.util.Result

/**
 * Represents an abstract async HTTP-request dispatcher.
 */
@InternalUseOnly
interface HttpClient {
    /**
     * Sends HTTP-request asynchronously.
     *
     * @param request request to be sent.
     * @param callback callback with a [Result] type to be invoked upon completion
     */
    fun <T : Any> send(
        request: HttpRequest<T>,
        callback: (Result<HttpResponse<T>>) -> Unit
    )
}

/**
 * Concrete implementation of the async HTTP-request dispatcher.
 *
 * @param [network] underlying HTTP-network implementation.
 * @param [networkQueue] execution queue used for sync request dispatching.
 * @param [retryPolicy] default retry policy for HTTP-request with no custom policy.
 * @param [listener] optional [HttpClientListener] for tracking status of the requests.
 */
@InternalUseOnly
class DefaultHttpClient(
    private val network: HttpNetwork,
    private val networkQueue: ExecutorQueue,
    private val callbackExecutor: Executor,
    private val retryPolicy: HttpRequestRetryPolicy,
    private val listener: HttpClientListener? = null,
    private val loggingInterceptor: HttpLoggingInterceptor? = null
) : HttpClient {
    //region Request sending

    /**
     * Sends HTTP-request asynchronously.
     *
     * @param request request to be sent.
     */
    override fun <T : Any> send(
        request: HttpRequest<T>,
        callback: (Result<HttpResponse<T>>) -> Unit
    ) {
        networkQueue.execute {
            // notify listener
            notifyOnStart(request)

            // send sync
            sendSync(request, callback)
        }
    }

    /**
     * Sends HTTP-request synchronously.
     *
     * @param request request to be sent.
     * @param callback callback with a [Result] type to be invoked upon completion
     */
    private fun <T> sendSync(
        request: HttpRequest<T>,
        callback: (Result<HttpResponse<T>>) -> Unit
    ) {
        try {
            val response = getResponseSync(request)
            if (response != null) {
                // notify listener
                notifyOnComplete(request)

                // invoke callback
                callbackExecutor.execute {
                    callback(Result.Success(response))
                }
            } else {
                // attempt another retry
                scheduleRetry(request, callback)
            }
        } catch (e: Exception) {
            // notify listener
            notifyOnComplete(request)

            // invoke callback
            callbackExecutor.execute {
                callback(Result.Error(e))
            }
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
        if (!network.isNetworkConnected()) {
            // should we retry?
            if (shouldRetry(request)) {
                return null
            }

            throw NetworkUnavailableException()
        }

        // log request
        loggingInterceptor?.intercept(request)

        // response body
        val networkResponse = network.performRequest(request)

        // log response
        loggingInterceptor?.intercept(networkResponse)

        // status
        val statusCode = networkResponse.statusCode
        val statusMessage = networkResponse.statusMessage

        // successful?
        if (statusCode in 200..299) {
            return HttpResponse(
                statusCode,
                statusMessage,
                request.readResponseObject(networkResponse),
                networkResponse.headers,
                networkResponse.duration
            )
        }

        // should we retry?
        if (shouldRetry(request, statusCode)) {
            return null
        }

        // give up
        val errorMessage = networkResponse.data.toString(Charsets.UTF_8)

        when (statusCode) {
            in 400..499 -> throw SendErrorException(statusCode, statusMessage, errorMessage)
            else -> throw UnexpectedResponseException(statusCode, statusMessage, errorMessage)
        }
    }

    //endregion

    //region Retry

    /**
     * @param statusCode HTTP-status code of the response or null for an exception.
     * @return true if [request] should be retried again
     */
    private fun <T> shouldRetry(request: HttpRequest<T>, statusCode: Int? = null): Boolean {
        return retryPolicy.shouldRetry(statusCode ?: UNDEFINED, request.numRetries)
    }

    /**
     * Schedules retry on the network queue according to retry policy.
     *
     * @param request request to be sent.
     * @param callback callback with a [Result] type to be invoked upon completion
     */
    private fun <T> scheduleRetry(
        request: HttpRequest<T>,
        callback: (Result<HttpResponse<T>>) -> Unit
    ) {
        val delay = retryPolicy.getRetryDelay(request.numRetries)
        loggingInterceptor?.retry(request, delay)

        networkQueue.execute(delay) {
            request.numRetries++

            // notify listener
            notifyOnRetry(request)

            // send request
            sendSync(request, callback)
        }
    }

    //endregion

    //region Listener notifications

    /** Notify onStart listener */
    private fun <T> notifyOnStart(request: HttpRequest<T>) {
        try {
            listener?.onRequestStart(this, request)
        } catch (e: Exception) {
            Log.e(LogTags.NETWORK, "Exception while notifying request start listener", e)
        }
    }

    /** Notify onRetry listener */
    private fun <T> notifyOnRetry(request: HttpRequest<T>) {
        try {
            listener?.onRequestRetry(this, request)
        } catch (e: Exception) {
            Log.e(LogTags.NETWORK, "Exception while notifying request retry listener", e)
        }
    }

    /** Notify onComplete listener */
    private fun <T> notifyOnComplete(request: HttpRequest<T>) {
        try {
            listener?.onRequestComplete(this, request)
        } catch (e: Exception) {
            Log.e(LogTags.NETWORK, "Exception while notifying request complete listener", e)
        }
    }

    //endregion
}

/**
 * A callback interface for request lifecycle events.
 */
@InternalUseOnly
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
