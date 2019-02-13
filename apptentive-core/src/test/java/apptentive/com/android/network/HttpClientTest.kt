package apptentive.com.android.network

import apptentive.com.android.TestCase
import apptentive.com.android.concurrent.ImmediateExecutionQueue
import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.atomic.AtomicBoolean

class HttpClientImplTest : TestCase() {
    private lateinit var network: MockHttpNetwork
    private lateinit var networkQueue: ImmediateExecutionQueue

    override fun setUp() {
        super.setUp()
        network = MockHttpNetwork()
        networkQueue = ImmediateExecutionQueue("network", dispatchManually = true)
    }

    @Test
    fun testStatusCodes() {
        val client = createHttpClient()
        sendRequest(client, MockHttpRequest("1"))
        sendRequest(client, MockHttpRequest("2", statusCode = 204))
        sendRequest(client, MockHttpRequest("3", statusCode = 500))
        sendRequest(client, MockHttpRequest("4", exceptionOnSend = true))
        sendRequest(client, MockHttpRequest("5", exceptionOnReceive = true))
        dispatchRequests()

        assertResults(
            "finished: 200",
            "finished: 204",
            "failed: Unexpected response 500 (Internal Server Error)",
            "failed: Exception while sending",
            "failed: Exception while receiving"
        )
    }

    @Test
    fun testResponseData() {
        val client = createHttpClient()

        val finished = AtomicBoolean(false)

        val expected = "Some test data with Unicode chars 文字"
        val request = MockHttpRequest(content = expected.toByteArray())
        request.onSuccess { _, res ->
            assertEquals(expected, res.content)
            finished.set(true)
        }
        client.send(request)
        dispatchRequests()

        assertTrue(finished.get())
    }

    //region Helpers

    private fun sendRequest(
        httpClient: HttpClient,
        request: HttpRequest<*>,
        retryPolicy: HttpRequestRetryPolicy? = null
    ) {
        request
            .onSuccess { _, res ->
                addResult("finished: ${res.statusCode}")
            }
            .onError { _, exception ->
                addResult("failed: ${exception.message}")
            }
        request.retryPolicy = retryPolicy
        httpClient.send(request)
    }

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

private object HttpRequestNoRetryPolicy : HttpRequestRetryPolicy {
    override fun shouldRetry(statusCode: Int, numRetries: Int) = false
    override fun getRetryDelay(numRetries: Int) = 0.0
}