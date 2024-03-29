package apptentive.com.android.network

import apptentive.com.android.TestCase
import apptentive.com.android.concurrent.ImmediateExecutor
import apptentive.com.android.concurrent.ImmediateExecutorQueue
import apptentive.com.android.util.Result
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import java.io.OutputStream

class HttpClientTest : TestCase() {
    private lateinit var network: MockHttpNetwork
    private lateinit var networkQueue: ImmediateExecutorQueue

    override fun setUp() {
        super.setUp()
        network = MockHttpNetwork()
        networkQueue = ImmediateExecutorQueue("network", dispatchManually = true)
    }

    //region Initialization tests

    /* Only POST and PUT requests can have bodies */
    @Test(expected = IllegalArgumentException::class)
    fun testGetRequestWithBody() {
        HttpRequest.Builder<String>("https://example.com")
            .method(HttpMethod.GET, FailureRequestBody)
            .responseReader(FailureResponseReader())
            .build()
        fail("Should not get this far")
    }

    /* POST and PUT requests can have empty bodies (allowed but bad practice) */
    @Test
    fun testPostRequestWithoutBody() {
        HttpRequest.Builder<String>("https://example.com")
            .get()
            .responseReader(FailureResponseReader())
            .build()
        // all good
    }

    //endregion

    //region Sending tests

    /* Different behaviour based on status code */
    @Test
    fun testSendingRequests() {
        val client = createHttpClient()
        sendRequest(client, createMockHttpRequest("1"))
        sendRequest(client, createMockHttpRequest("2", statusCode = 204))
        sendRequest(client, createMockHttpRequest("3", statusCode = 500))
        sendRequest(client, createMockHttpRequest("4", exceptionOnSend = true))
        sendRequest(client, createMockHttpRequest("5", exceptionOnReceive = true))
        dispatchRequests()

        assertResults(
            "1 finished: 200",
            "2 finished: 204",
            "3 failed: 500 (Internal Server Error)",
            "4 exception: failed to send",
            "5 exception: failed to receive"
        )
    }

    /* Fails gracefully if network is missing */
    @Test
    fun testSendingRequestNoNetwork() {
        val client = createHttpClient(networkConnected = false)
        sendRequest(client, createMockHttpRequest("request"))
        dispatchRequests()

        assertResults("request failed: no network")
    }

    /* Fails with [UnexpectedResponseException] if request was not successful */
    @Test
    fun testUnexpectedResponseCode() {
        val client = createHttpClient()
        sendRequest(client, createMockHttpRequest("request", statusCode = 500))
        dispatchRequests()

        assertResults("request failed: 500 (Internal Server Error)")
    }

    //endregion

    //region Response data tests

    /* Callbacks must be properly executed */
    @Test
    fun testResponseData() {
        val client = createHttpClient()

        val expected = "Some test data with Unicode chars 文字"
        var actual: String? = null

        client.send(createMockHttpRequest(response = expected)) {
            if (it is Result.Success) {
                actual = it.data.payload
            }
        }
        dispatchRequests()

        assertEquals(expected, actual)
    }

    //endregion

    //region Json request tests

    /**
     * Json-requests tests
     */
    /* Should properly decode json responses */
    @Test
    fun testJsonResponse() {
        val expected = MyResponse("value")

        val client = createHttpClient()
        val request = createMockJsonRequest(
            responseObject = expected
        )

        var actual: MyResponse? = null
        client.send(request) {
            if (it is Result.Success) {
                actual = it.data.payload
            }
        }
        dispatchRequests()

        assertEquals(expected, actual)
    }

    //endregion

    //region Retry logic tests

    /* Should retry on 5xx */
    @Test
    fun testRetryServerError() {
        val client = createHttpClientForRetry()

        sendRequest(client, createMockHttpRequest("request", statusCode = 500))

        dispatchRequests()
        assertResults("request start")

        dispatchRequests()
        assertResults("request retry: 1")

        dispatchRequests()
        assertResults(
            "request retry: 2",
            "request complete",
            "request failed: 500 (Internal Server Error)"
        )

        dispatchRequests()
        assertResults()
    }

    /* Should not retry on 4xx */
    @Test
    fun testRetryAuthError() {
        val client = createHttpClientForRetry()

        sendRequest(client, createMockHttpRequest("request", statusCode = 401))

        dispatchRequests()
        assertResults(
            "request start",
            "request complete",
            "request exception: Send error: 401 (Unauthorized)"
        )

        dispatchRequests()
        assertResults()
    }

    /* Should not retry on send exception */
    @Test
    fun testRetryOnSendException() {
        val client = createHttpClientForRetry()

        sendRequest(client, createMockHttpRequest("request", exceptionOnSend = true))

        dispatchRequests()
        assertResults(
            "request start",
            "request complete",
            "request exception: failed to send"
        )

        dispatchRequests()
        assertResults()
    }

    /* Should not retry on receive exception */
    @Test
    fun testRetryOnReceiveException() {
        val client = createHttpClientForRetry()

        sendRequest(client, createMockHttpRequest("request", exceptionOnReceive = true))

        dispatchRequests()
        assertResults(
            "request start",
            "request complete",
            "request exception: failed to receive"
        )

        dispatchRequests()
        assertResults()
    }

    /* Should retry on a disconnected network */
    @Test
    fun testRetryOnDisconnectedNetwork() {
        val client = createHttpClientForRetry()

        network.networkConnected = false
        sendRequest(client, createMockHttpRequest("request"))

        dispatchRequests()
        assertResults("request start")

        dispatchRequests()
        assertResults("request retry: 1")

        dispatchRequests()
        assertResults(
            "request retry: 2",
            "request complete",
            "request failed: no network"
        )

        dispatchRequests()
        assertResults()
    }

    /* Should succeed when network is back */
    @Test
    fun testRetryRestoredNetwork() {
        val client = createHttpClientForRetry()

        network.networkConnected = false
        sendRequest(client, createMockHttpRequest("request"))

        dispatchRequests()
        assertResults("request start")

        dispatchRequests()
        assertResults("request retry: 1")

        network.networkConnected = true

        dispatchRequests()
        assertResults(
            "request retry: 2",
            "request complete",
            "request finished: 200"
        )

        dispatchRequests()
        assertResults()
    }

    /* Should be successful at the end */
    @Test
    fun testRetrySuccess() {
        val client = createHttpClientForRetry()

        val responses = arrayOf(
            createNetworkResponse(statusCode = 500), // d'oh!
            createNetworkResponse(statusCode = 500), // d'oh!
            createNetworkResponse(statusCode = 200) // woo-hoo!
        )

        sendRequest(
            client,
            createMockHttpRequest(
                tag = "request",
                responses = responses
            )
        )

        dispatchRequests()
        assertResults("request start")

        dispatchRequests()
        assertResults("request retry: 1")

        dispatchRequests()
        assertResults(
            "request retry: 2",
            "request complete",
            "request finished: 200"
        )

        dispatchRequests()
        assertResults()
    }

    //endregion

    //region Helpers

    /**
     * Sends request and captures the result for checking.
     */
    private fun <T : Any> sendRequest(
        httpClient: HttpClient,
        request: HttpRequest<T>
    ) {
        httpClient.send(request) {
            when (it) {
                is Result.Success -> addResult("${request.tag} finished: ${it.data.statusCode}")
                is Result.Error -> {
                    val message =
                        when (val exception = it.error) {
                            is NetworkUnavailableException -> "failed: no network"
                            is UnexpectedResponseException -> "failed: ${exception.statusCode} (${exception.statusMessage})"
                            else -> "exception: ${exception.message}"
                        }
                    addResult("${request.tag} $message")
                }
            }
        }
    }

    /**
     * Creates HttpClient for testing retry logic.
     */
    private fun createHttpClientForRetry(): HttpClient {
        return createHttpClient(
            retryPolicy = DefaultHttpRequestRetryPolicy(maxNumRetries = 2),
            listener = mockClientListener
        )
    }

    /**
     * Creates HttpClient with mock network.
     */
    private fun createHttpClient(
        networkConnected: Boolean = true,
        retryPolicy: HttpRequestRetryPolicy = HttpRequestNoRetryPolicy,
        listener: HttpClientListener? = null
    ): HttpClient {
        network.networkConnected = networkConnected
        return DefaultHttpClient(
            network = network,
            networkQueue = networkQueue,
            retryPolicy = retryPolicy,
            listener = listener,
            callbackExecutor = ImmediateExecutor
        )
    }

    /**
     * Dispatches queued requests.
     */
    private fun dispatchRequests() {
        networkQueue.dispatchAll()
    }

    //endregion

    //region Objects

    /**
     * Mock HttpClient listener for capturing request states.
     */
    private val mockClientListener = object : HttpClientListener {
        override fun onRequestStart(client: HttpClient, request: HttpRequest<*>) {
            addResult("${request.tag} start")
        }

        override fun onRequestRetry(client: HttpClient, request: HttpRequest<*>) {
            addResult("${request.tag} retry: ${request.numRetries}")
        }

        override fun onRequestComplete(client: HttpClient, request: HttpRequest<*>) {
            addResult("${request.tag} complete")
        }
    }

    //endregion
}

/**
 * Always fails serialization.
 */
private object FailureRequestBody : HttpRequestBody {
    override val contentType: String
        get() = "text/plain"

    override fun write(stream: OutputStream) {
        throw AssertionError("Failed to deserialize")
    }
}

/**
 * Always fails deserialization.
 */
private class FailureResponseReader<T> : HttpResponseReader<T> {
    override fun read(response: HttpNetworkResponse): T =
        throw AssertionError("Failed to serialize")
}

/**
 * Retry policy for immediate failure.
 */
private object HttpRequestNoRetryPolicy : HttpRequestRetryPolicy {
    override fun shouldRetry(statusCode: Int, numRetries: Int) = false
    override fun getRetryDelay(numRetries: Int) = 0.0
}

/* For json request testing */
private data class MyResponse(val value: String)
