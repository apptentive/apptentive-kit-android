package apptentive.com.android.feedback.backend

import apptentive.com.android.core.getTimeSeconds
import apptentive.com.android.feedback.BuildConfig
import apptentive.com.android.feedback.model.AppRelease
import apptentive.com.android.feedback.model.Configuration
import apptentive.com.android.feedback.model.Device
import apptentive.com.android.feedback.model.EngagementManifest
import apptentive.com.android.feedback.model.MessageList
import apptentive.com.android.feedback.model.Person
import apptentive.com.android.feedback.model.SDK
import apptentive.com.android.feedback.payload.PayloadData
import apptentive.com.android.network.CacheControl
import apptentive.com.android.network.HttpByteArrayResponseReader
import apptentive.com.android.network.HttpClient
import apptentive.com.android.network.HttpHeaders
import apptentive.com.android.network.HttpHeaders.Companion.CACHE_CONTROL
import apptentive.com.android.network.HttpJsonResponseReader
import apptentive.com.android.network.HttpMethod
import apptentive.com.android.network.HttpNetworkResponse
import apptentive.com.android.network.HttpRequest
import apptentive.com.android.network.HttpResponseReader
import apptentive.com.android.network.MutableHttpHeaders
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.CONVERSATION
import apptentive.com.android.util.Result

internal class DefaultConversationService(
    private val httpClient: HttpClient,
    apptentiveKey: String,
    apptentiveSignature: String,
    apiVersion: Int,
    sdkVersion: String,
    private val baseURL: String
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
        device: Device,
        sdk: SDK,
        appRelease: AppRelease,
        person: Person,
        callback: (Result<ConversationCredentials>) -> Unit
    ) {
        val request = createJsonRequest<ConversationCredentials>(
            method = HttpMethod.POST,
            path = "conversation",
            body = ConversationTokenRequestData.from(device, sdk, appRelease, person)
        )
        sendRequest(request, callback)
    }

    override fun fetchEngagementManifest(
        conversationToken: String,
        conversationId: String,
        callback: (Result<EngagementManifest>) -> Unit
    ) {
        val request = createJsonRequest(
            method = HttpMethod.GET,
            path = "conversations/$conversationId/interactions",
            headers = MutableHttpHeaders().apply {
                this["Authorization"] = "Bearer $conversationToken"
            },
            responseReader = EngagementManifestReader
        )
        sendRequest(request, callback)
    }

    override fun fetchConfiguration(
        conversationToken: String,
        conversationId: String,
        callback: (Result<Configuration>) -> Unit
    ) {
        val request = createJsonRequest(
            method = HttpMethod.GET,
            path = "conversations/$conversationId/configuration",
            headers = MutableHttpHeaders().apply {
                this["Authorization"] = "Bearer $conversationToken"
            },
            responseReader = ConfigurationReader
        )
        sendRequest(request, callback)
    }

    override fun getMessages(
        conversationToken: String,
        conversationId: String,
        lastMessageID: String,
        callback: (Result<MessageList>) -> Unit
    ) {
        val debugPageSize = 5
        val path = if (BuildConfig.DEBUG)
            "conversations/$conversationId/messages?starts_after=$lastMessageID&page_size=$debugPageSize"
        else
            "conversations/$conversationId/messages?starts_after=$lastMessageID" // Takes default page size set by the server
        val request = createJsonRequest(
            method = HttpMethod.GET,
            path = path,
            headers = MutableHttpHeaders().apply {
                this["Authorization"] = "Bearer $conversationToken"
            },
            responseReader = HttpJsonResponseReader(MessageList::class.java)
        )
        sendRequest(request, callback)
    }

    override fun getAttachment(remoteUrl: String, callback: (Result<ByteArray>) -> Unit) {
        val request = HttpRequest
            .Builder<ByteArray>(remoteUrl)
            .method(HttpMethod.GET, null)
            .responseReader(HttpByteArrayResponseReader())
            .build()

        httpClient.send(request) {
            when (it) {
                is Result.Success -> callback(Result.Success(it.data.payload))
                is Result.Error -> callback(it)
            }
        }
    }

    override fun sendPayloadRequest(
        payload: PayloadData,
        conversationId: String,
        conversationToken: String,
        callback: (Result<PayloadResponse>) -> Unit
    ) {
        val url = createURL(payload.resolvePath(conversationId))
        val request = HttpRequest.Builder<PayloadResponse>(url)
            .method(payload.method, payload.data, contentType = payload.mediaType.toString())
            .headers(defaultHeaders)
            .header("Authorization", "Bearer $conversationToken")
            .responseReader(HttpJsonResponseReader(PayloadResponse::class.java))
            .build()
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

    private fun createURL(path: String): String {
        return if (path.startsWith("/")) "$baseURL$path" else "$baseURL/$path"
    }
}

private object EngagementManifestReader :
    HttpResponseReader<EngagementManifest> {
    override fun read(
        response: HttpNetworkResponse
    ): EngagementManifest {
        val cacheControl = parseCacheControl(response.headers[CACHE_CONTROL]?.value)
        val manifest = HttpJsonResponseReader(EngagementManifest::class.java).read(response)
        return manifest.copy(expiry = getTimeSeconds() + cacheControl.maxAgeSeconds)
    }

    private fun parseCacheControl(value: String?): CacheControl {
        if (value != null) {
            try {
                return CacheControl.parse(value)
            } catch (e: Exception) {
                Log.e(CONVERSATION, "Unable to parse cache control value: $value", e)
            }
        }
        return CacheControl()
    }
}

private object ConfigurationReader : HttpResponseReader<Configuration> {
    override fun read(response: HttpNetworkResponse): Configuration {
        val cacheControl = parseCacheControl(response.headers[CACHE_CONTROL]?.value)
        val configuration = HttpJsonResponseReader(Configuration::class.java).read(response)
        return configuration.copy(expiry = getTimeSeconds() + cacheControl.maxAgeSeconds)
    }

    private fun parseCacheControl(value: String?): CacheControl {
        if (value != null) {
            try {
                return CacheControl.parse(value)
            } catch (e: Exception) {
                Log.e(CONVERSATION, "Unable to parse cache control value: $value", e)
            }
        }
        return CacheControl()
    }
}
