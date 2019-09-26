package apptentive.com.android.feedback.backend

import apptentive.com.android.concurrent.Executor
import apptentive.com.android.network.*
import apptentive.com.android.util.Result

class DefaultConversationService(
    private val httpClient: HttpClient,
    apptentiveKey: String,
    apptentiveSignature: String,
    apiVersion: Int,
    sdkVersion: String,
    private val baseURL: String,
    private val callbackExecutor: Executor
) : ConversationService {
    private val defaultHeaders = MutableHttpHeaders().apply {
        this["User-Agent"] = "Apptentive/$sdkVersion (Android)"
        this["Connection"] = "Keep-Alive"
        this["Accept-Encoding"] = "gzip"
        this["Accept"] = "application/json"
        this["APPTENTIVE-KEY"] = apptentiveKey
        this["APPTENTIVE-SIGNATURE"] = apptentiveSignature
        this["X-API-Version"] = apiVersion.toString()
    }

    override fun fetchConversationToken(
        payload: ConversationTokenFetchBody,
        callback: (Result<ConversationTokenFetchResponse>) -> Unit
    ) {
        val request = createJsonRequest<ConversationTokenFetchResponse>(
            method = HttpMethod.POST,
            path = "/conversation",
            body = payload
        )
        sendRequest(request, callback)
    }

    private fun <T : Any> sendRequest(request: HttpRequest<T>, callback: (Result<T>) -> Unit) {
        httpClient.send(request) {
            when (it) {
                is Result.Success -> callback(Result.Success(it.data.payload))
                is Result.Error -> callback(it)
            }
        }
    }

    private inline fun <reified T> createJsonRequest(
        method: HttpMethod,
        path: String,
        body: Any? = null,
        headers: HttpHeaders? = null
    ): HttpRequest<T> {
        val allHeaders = MutableHttpHeaders()
        allHeaders.addAll(defaultHeaders)
        if (headers != null) {
            allHeaders.addAll(headers)
        }

        return HttpRequest.Builder<T>()
            .url(createURL(path))
            .method(method, body)
            .headers(allHeaders)
            .responseReader(HttpJsonResponseReader(T::class.java))
            .callbackOn(callbackExecutor)
            .build()
    }

    private fun createURL(path: String) = "$baseURL/$path"
}