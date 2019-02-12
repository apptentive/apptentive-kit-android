package apptentive.com.android.network

import apptentive.com.android.concurrent.ImmediateExecutionQueue
import junit.framework.TestCase
import org.junit.Ignore

class HttpClientImplTest : TestCase() {
    private lateinit var network: MockHttpNetwork
    private lateinit var networkQueue: ImmediateExecutionQueue

    override fun setUp() {
        super.setUp()
        network = MockHttpNetwork()
        networkQueue = ImmediateExecutionQueue("network", dispatchManually = true)
    }

    @Ignore("Implement me!")
    fun testSend() {
        val client = createHttpClient()
        // TODO: send request
    }

    //region Helpers

    private fun createHttpClient(retryPolicy: HttpRequestRetryPolicy? = null): HttpClient {
        return HttpClientImpl(
            network,
            networkQueue,
            retryPolicy ?: HttpRequestNoRetryPolicy
        )
    }

    private fun dispatchRequests() {
        networkQueue.dispatchAll()
    }

    //endregion
}

private class MockHttpRequest(
    val mockResponse: HttpResponse,
    method: HttpMethod = HttpMethod.GET
) : HttpRequest(method, "https://example.com") {
    override fun createRequestBody(): ByteArray? = mockResponse.content
    override fun parseResponseBody(bytes: ByteArray) = Unit
}

private class MockHttpNetwork : HttpNetwork {
    var networkConnected: Boolean = true

    override val isNetworkConnected: Boolean get() = networkConnected

    override fun performRequest(request: HttpRequest): HttpResponse = (request as MockHttpRequest).mockResponse
}

private object HttpRequestNoRetryPolicy : HttpRequestRetryPolicy {
    override fun shouldRetry(request: HttpRequest) = false
    override fun getRetryDelay(request: HttpRequest) = 0.0
}