package apptentive.com.android.network

import apptentive.com.android.concurrent.ExecutionQueue
import apptentive.com.android.concurrent.Promise
import apptentive.com.android.concurrent.PromiseImpl
import apptentive.com.android.core.TimeInterval
import apptentive.com.android.core.toMilliseconds
import apptentive.com.android.util.Log
import apptentive.com.android.util.StreamUtils
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.GZIPInputStream

class HttpClient(
    private val network: HttpNetwork,
    private val networkQueue: ExecutionQueue,
    private val dispatchQueue: ExecutionQueue
) {
    fun <T : HttpRequest> send(request: T): Promise<T> {
        val promise = PromiseImpl<T>(dispatchQueue)
        networkQueue.dispatch {
            try {
                sendSync(request)
                promise.onValue(request)
            } catch (e: Exception) {
                promise.onError(e)
            }
        }
        return promise
    }

    private fun sendSync(request: HttpRequest) {
        val response = network.performRequest(request)
    }
}