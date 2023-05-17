package apptentive.com.android.feedback.backend

import apptentive.com.android.TestCase
import apptentive.com.android.concurrent.ImmediateExecutorQueue
import apptentive.com.android.core.TimeInterval
import apptentive.com.android.feedback.mockAppRelease
import apptentive.com.android.feedback.mockDevice
import apptentive.com.android.feedback.mockPerson
import apptentive.com.android.feedback.mockSdk
import apptentive.com.android.feedback.payload.MediaType
import apptentive.com.android.feedback.payload.PayloadData
import apptentive.com.android.feedback.payload.PayloadType
import apptentive.com.android.network.DefaultHttpClient
import apptentive.com.android.network.DefaultHttpRequestRetryPolicy
import apptentive.com.android.network.HttpHeaders
import apptentive.com.android.network.HttpMethod
import apptentive.com.android.network.HttpNetwork
import apptentive.com.android.network.HttpNetworkResponse
import apptentive.com.android.network.HttpRequest
import apptentive.com.android.network.HttpRequestBody
import apptentive.com.android.serialization.json.JsonConverter
import apptentive.com.android.serialization.json.JsonException
import apptentive.com.android.util.Result
import com.google.common.truth.Truth.assertThat
import org.junit.Ignore
import org.junit.Test
import java.io.ByteArrayOutputStream

class DefaultConversationServiceTest : TestCase() {

    @Test
    fun fetchConversationToken() {
        val network = MockHttpNetwork().apply {
            register("https://api.apptentive.com/conversation") { request ->

                // this is what we expect to be sent from the client
                val expected = Request(
                    method = HttpMethod.POST,
                    headers = request.headers,
                    payload = mapOf(
                        "device" to mapOf(
                            "uuid" to "uuid",
                            "os_name" to mockDevice.osName,
                            "os_version" to mockDevice.osVersion,
                            "os_build" to mockDevice.osBuild,
                            "os_api_level" to mockDevice.osApiLevel.toString(),
                            "manufacturer" to mockDevice.manufacturer,
                            "model" to mockDevice.model,
                            "board" to mockDevice.board,
                            "product" to mockDevice.product,
                            "brand" to mockDevice.brand,
                            "cpu" to mockDevice.cpu,
                            "device" to mockDevice.device,
                            "carrier" to mockDevice.carrier,
                            "current_carrier" to mockDevice.currentCarrier,
                            "network_type" to mockDevice.networkType,
                            "build_type" to mockDevice.buildType,
                            "build_id" to mockDevice.buildId,
                            "bootloader_version" to mockDevice.bootloaderVersion,
                            "radio_version" to mockDevice.radioVersion,
                            "locale_country_code" to mockDevice.localeCountryCode,
                            "locale_language_code" to mockDevice.localeLanguageCode,
                            "locale_raw" to mockDevice.localeRaw,
                            "utc_offset" to mockDevice.utcOffset.toString(),
                            "custom_data" to mapOf("key" to "value"),
                            "integration_config" to mapOf(
                                "apptentive" to mapOf("apptentive_key" to "apptentive_value"),
                                "amazon_aws_sns" to mapOf("amazon_key" to "amazon_value"),
                                "urban_airship" to mapOf("urban_key" to "urban_value")
                            ),
                            "nonce" to "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
                        ),
                        "app_release" to mapOf(
                            "sdk_nonce" to "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
                            "sdk_author_email" to mockSdk.authorEmail,
                            "sdk_author_name" to mockSdk.authorName,
                            "sdk_distribution" to mockSdk.distribution,
                            "sdk_distribution_version" to mockSdk.distributionVersion,
                            "sdk_platform" to mockSdk.platform,
                            "sdk_programming_language" to mockSdk.programmingLanguage,
                            "sdk_version" to mockSdk.version,
                            "nonce" to "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
                            "app_store" to mockAppRelease.appStore,
                            "debug" to mockAppRelease.debug,
                            "identifier" to mockAppRelease.identifier,
                            "inheriting_styles" to mockAppRelease.inheritStyle,
                            "overriding_styles" to mockAppRelease.overrideStyle,
                            "target_sdk_version" to mockAppRelease.targetSdkVersion,
                            "min_sdk_version" to mockAppRelease.minSdkVersion,
                            "type" to mockAppRelease.type,
                            "version_code" to mockAppRelease.versionCode.toDouble(),
                            "version_name" to mockAppRelease.versionName
                        ),
                        "person" to mapOf(
                            "mparticle_id" to mockPerson.mParticleId,
                            "custom_data" to mapOf("person_key" to "person_value")
                        )
                    )
                )

                assertThat(expected).isEqualTo(request)

                // this is a typical response from the backend
                Response(
                    body = mapOf(
                        "id" to "conversation_id",
                        "device_id" to "device_id",
                        "person_id" to "person_id",
                        "token" to "token",
                        "encryption_key" to "encryption_key"
                    )
                )
            }
        }
        val service = createConversationService(network)

        service.fetchConversationToken(
            device = mockDevice,
            sdk = mockSdk,
            appRelease = mockAppRelease,
            person = mockPerson
        ) {
            when (it) {
                is Result.Success -> {
                    with(it.data) {
                        assertThat(id).isEqualTo("conversation_id")
                        assertThat(deviceId).isEqualTo("device_id")
                        assertThat(personId).isEqualTo("person_id")
                        assertThat(token).isEqualTo("token")
                        assertThat(encryptionKey).isEqualTo("encryption_key")
                    }
                }
                is Result.Error -> throw AssertionError(it.error)
            }
        }
    }

    @Test
    @Ignore
    fun fetchEngagementManifest() {
        TODO("Implement me")
    }

    @Test
    fun sendPayloadRequest() {
        val conversationId = "conversation_id"
        val conversationToken = "conversation_token"
        val nonce = "nonce"

        val network = MockHttpNetwork().apply {
            register("https://api.apptentive.com/conversations/$conversationId/events") { request ->
                // this is what we expect to be sent from the client
                val expected = Request(
                    method = HttpMethod.POST,
                    headers = request.headers,
                    body = "Payload Data".toByteArray()
                )

                assertThat(expected).isEqualTo(request)

                // the response is empty
                Response(
                    body = emptyMap<String, Any>()
                )
            }
        }
        val payload = PayloadData(
            nonce = nonce,
            type = PayloadType.Event,
            path = "/conversations/:conversation_id/events",
            method = HttpMethod.POST,
            mediaType = MediaType.applicationJson,
            data = "Payload Data".toByteArray()
        )

        val service = createConversationService(network)
        service.sendPayloadRequest(
            payload = payload,
            conversationToken = conversationToken,
            conversationId = conversationId
        ) {
            assertThat(it is Result.Success).isTrue()
        }
    }

    private fun createConversationService(network: MockHttpNetwork): ConversationService {
        val httpClient = DefaultHttpClient(
            network = network,
            networkQueue = ImmediateExecutorQueue(),
            callbackExecutor = ImmediateExecutorQueue(),
            retryPolicy = DefaultHttpRequestRetryPolicy()
        )

        return DefaultConversationService(
            httpClient = httpClient,
            apptentiveKey = "key",
            apptentiveSignature = "signature",
            apiVersion = 9,
            sdkVersion = "6.0.0",
            baseURL = "https://api.apptentive.com"
        )
    }
}

private class MockHttpNetwork : HttpNetwork {
    private val lookup = mutableMapOf<String, (Request) -> Response>()

    override fun isNetworkConnected() = true

    fun register(url: String, handler: (Request) -> Response) {
        lookup[url] = handler
    }

    override fun performRequest(request: HttpRequest<*>): HttpNetworkResponse {
        val url = request.url.toString()
        val handler = lookup[url]
        if (handler != null) {
            val payload = request.requestBody.toJson()
            val data = if (payload == null) request.requestBody.toByteArray() else null
            val response = handler.invoke(
                Request(
                    method = request.method,
                    headers = request.headers,
                    payload = payload,
                    body = data
                )
            )
            return HttpNetworkResponse(
                statusCode = response.statusCode,
                statusMessage = response.statusMessage,
                data = JsonConverter.toJson(response.body).toByteArray(),
                headers = response.headers,
                duration = response.duration
            )
        }
        throw AssertionError("No handler for URL: $url")
    }
}

private data class Request(
    val method: HttpMethod,
    val headers: HttpHeaders = HttpHeaders(),
    val body: ByteArray? = null,
    val payload: Map<String, *>? = null
) {
    init {
        require(body != null || payload != null)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Request

        if (method != other.method) return false
        if (headers != other.headers) return false
        if (body != null) {
            if (other.body == null) return false
            if (!body.contentEquals(other.body)) return false
        } else if (other.body != null) return false
        if (payload != other.payload) return false

        return true
    }

    override fun hashCode(): Int {
        var result = method.hashCode()
        result = 31 * result + headers.hashCode()
        result = 31 * result + (body?.contentHashCode() ?: 0)
        result = 31 * result + (payload?.hashCode() ?: 0)
        return result
    }
}

private data class Response(
    val statusCode: Int = 200,
    val statusMessage: String = "OK",
    val headers: HttpHeaders = HttpHeaders(),
    val body: Map<String, *> = mapOf<String, Any>(),
    val duration: TimeInterval = 1.0
)

fun HttpRequestBody?.toJson(): Map<String, *>? {
    if (this != null) {
        val stream = ByteArrayOutputStream()
        stream.use {
            write(stream)
        }
        val bytes = stream.toByteArray()
        val json = String(bytes)
        try {
            return JsonConverter.toMap(json)
        } catch (e: JsonException) {
            // ignore
        }
    }
    return null
}

fun HttpRequestBody?.toByteArray(): ByteArray {
    if (this != null) {
        val stream = ByteArrayOutputStream()
        stream.use {
            write(stream)
        }
        return stream.toByteArray()
    }
    return ByteArray(0)
}
