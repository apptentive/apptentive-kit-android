package apptentive.com.android.feedback.payload

import apptentive.com.android.network.*
import apptentive.com.android.util.Result

class ConversationPayloadService(
    private val httpClient: HttpClient,
    apptentiveKey: String,
    apptentiveSignature: String,
    apiVersion: Int,
    sdkVersion: String,
    private val baseURL: String,
    val conversationId: String,
    val conversationToken: String
) : PayloadService {
    override fun sendPayload(payload: Payload, callback: (Result<Payload>) -> Unit) {
        val request = createJsonRequest<Payload>(
            method = getMethodTypeFromPayloadType(payload),
            path = getPathFromPayloadType(payload, conversationId),
            body = payload.data,
            headers = getConversationTokenHeader(conversationToken)
        )
        sendRequest(request, callback)
    }

    private fun getConversationTokenHeader(conversationToken: String): HttpHeaders? {
        return MutableHttpHeaders().apply {
            this["Authorization"] = "Bearer $conversationToken"
        }
    }

    internal fun getMethodTypeFromPayloadType(payload: Payload) : HttpMethod {
        val payloadTypeToHttpMethodMap = mapOf(
            PayloadType.Person to HttpMethod.PUT,
            PayloadType.Device to HttpMethod.PUT,
            PayloadType.AppRelease to HttpMethod.PUT,
            PayloadType.Event to HttpMethod.POST
        )

        return payloadTypeToHttpMethodMap[payload.type] ?: HttpMethod.POST
    }


    internal fun getPathFromPayloadType(payload: Payload, conversationId: String): String {
        val payloadTypeToPathMap = mapOf(
            PayloadType.Person to "person",
            PayloadType.Device to "device",
            PayloadType.AppRelease to "app_release",
            PayloadType.Event to "events"
        )
        return "/conversations/$conversationId/${payloadTypeToPathMap[payload.type]}"
    }

    private val defaultHeaders = MutableHttpHeaders().apply {
        this["User-Agent"] = "Apptentive/$sdkVersion (Android)"
        this["Connection"] = "Keep-Alive"
        this["Accept-Encoding"] = "gzip"
        this["Accept"] = "application/json"
        this["APPTENTIVE-KEY"] = apptentiveKey
        this["APPTENTIVE-SIGNATURE"] = apptentiveSignature
        this["X-API-Version"] = apiVersion.toString()
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
        headers: HttpHeaders? = null,
        responseReader: HttpResponseReader<T> = HttpJsonResponseReader(T::class.java)
    ): HttpRequest<T> {
        val allHeaders = MutableHttpHeaders()
        allHeaders.addAll(defaultHeaders)
        if (headers != null) {
            allHeaders.addAll(headers)
        }

        return HttpRequest.Builder<T>(createURL(path))
            .method(method, body)
            .headers(allHeaders)
            .responseReader(responseReader)
            .build()
    }

    private fun createURL(path: String) = "$baseURL/$path"

}