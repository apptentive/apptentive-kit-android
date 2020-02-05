package apptentive.com.android.feedback.payload

import apptentive.com.android.TestCase
import apptentive.com.android.concurrent.ImmediateExecutorQueue
import apptentive.com.android.core.TimeInterval
import apptentive.com.android.feedback.backend.toJson
import apptentive.com.android.network.*
import apptentive.com.android.serialization.json.JsonConverter
import com.google.common.truth.Truth
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream

class ConversationPayloadServiceTest : TestCase() {

    private lateinit var network: MockHttpNetwork
    private lateinit var httpClient: DefaultHttpClient
    private lateinit var service: ConversationPayloadService

    @Before
    fun setup() {
        network = MockHttpNetwork()

        httpClient = DefaultHttpClient(
            network = network,
            networkQueue = ImmediateExecutorQueue(),
            callbackExecutor = ImmediateExecutorQueue(),
            retryPolicy = DefaultHttpRequestRetryPolicy()
        )

        service = ConversationPayloadService(
            httpClient,
            apptentiveKey = "key",
            apptentiveSignature = "signature",
            apiVersion = 9,
            sdkVersion = "6.0.0",
            baseURL = "https://api.apptentive.com",
            conversationToken = "conversation_token",
            conversationId = "conversation_id"
        )
    }

    @Test
    fun testGetPathFromPayloadType() {
        var expected = "/conversations/conversation_id/events"

        Truth.assertThat(expected).isEqualTo(
            service.getPathFromPayloadType(
                createPayload("payload -1", PayloadType.Event),
                "conversation_id"
            )
        )

        expected = "/conversations/conversation_id/person"

        Truth.assertThat(expected).isEqualTo(
            service.getPathFromPayloadType(
                createPayload("payload -2", PayloadType.Person),
                "conversation_id"
            )
        )

        expected = "/conversations/conversation_id/device"

        Truth.assertThat(expected).isEqualTo(
            service.getPathFromPayloadType(
                createPayload("payload -3", PayloadType.Device),
                "conversation_id"
            )
        )

        expected = "/conversations/conversation_id/app_release"

        Truth.assertThat(expected).isEqualTo(
            service.getPathFromPayloadType(
                createPayload("payload -4", PayloadType.AppRelease),
                "conversation_id"
            )
        )
    }

    @Test
    fun testGetMethodTypeFromPayloadType() {
        var expected = HttpMethod.POST

        Truth.assertThat(expected).isEqualTo(
            service.getMethodTypeFromPayloadType(
                createPayload("payload -1", PayloadType.Event)
            )
        )

        expected = HttpMethod.PUT

        Truth.assertThat(expected).isEqualTo(
            service.getMethodTypeFromPayloadType(
                createPayload("payload -2", PayloadType.Person)
            )
        )

        Truth.assertThat(expected).isEqualTo(
            service.getMethodTypeFromPayloadType(
                createPayload("payload -3", PayloadType.Device)
            )
        )

        Truth.assertThat(expected).isEqualTo(
            service.getMethodTypeFromPayloadType(
                createPayload("payload -4", PayloadType.AppRelease)
            )
        )
    }

    private fun createPayload(
        nonce: String,
        payloadType: PayloadType,
        data: String = "Payload data"
    ) = Payload(
        nonce = nonce,
        type = payloadType,
        mediaType = MediaType.applicationJson,
        data = data.toByteArray()
    )
}

private class MockHttpNetwork : HttpNetwork {
    private val lookup = mutableMapOf<String, (Request) -> Response>()

    override fun isNetworkConnected() = true

    override fun performRequest(request: HttpRequest<*>): HttpNetworkResponse {
        val url = request.url.toString()
        val handler = lookup[url]
        if (handler != null) {
            val response = handler.invoke(
                Request(
                    method = request.method,
                    headers = request.headers,
                    body = request.requestBody.toJson()
                )
            )
            return HttpNetworkResponse(
                statusCode = response.statusCode,
                statusMessage = response.statusMessage,
                stream = ByteArrayInputStream(JsonConverter.toJson(response.body).toByteArray()),
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
    val body: Map<String, *> = mapOf<String, Any>()
)

private data class Response(
    val statusCode: Int = 200,
    val statusMessage: String = "OK",
    val headers: HttpHeaders = HttpHeaders(),
    val body: Map<String, *> = mapOf<String, Any>(),
    val duration: TimeInterval = 1.0
)