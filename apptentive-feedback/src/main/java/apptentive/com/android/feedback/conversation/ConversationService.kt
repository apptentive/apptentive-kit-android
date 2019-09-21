package apptentive.com.android.feedback.conversation

import apptentive.com.android.concurrent.Executor
import apptentive.com.android.feedback.backend.ConversationFetchService
import apptentive.com.android.feedback.backend.ConversationTokenFetchBody
import apptentive.com.android.feedback.backend.ConversationTokenFetchResponse
import apptentive.com.android.network.*
import apptentive.com.android.util.Callback

class ConversationService(
    private val httpClient: HttpClient,
    apptentiveKey: String,
    apptentiveSignature: String,
    apiVersion: Int,
    sdkVersion: String,
    private val baseURL: String,
    private val callbackExecutor: Executor
) : ConversationFetchService {
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
        callback: Callback<ConversationTokenFetchResponse>
    ) {
        val request = createJsonRequest<ConversationTokenFetchResponse>(
            method = HttpMethod.POST,
            path = "/conversation",
            body = payload
        )
        sendRequest(request, callback)
    }

    private fun <T> sendRequest(request: HttpRequest<T>, callback: Callback<T>) {
        httpClient.send(
            request = request,
            onValue = {
                val payload = it.payload
                callback.onComplete(payload)
            },
            onError = {
                callback.onFailure(it)
            })
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