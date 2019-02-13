package apptentive.com.android.network

/**
 * Custom [HttpNetwork] implementation for sync unit testing.
 */
class MockHttpNetwork : HttpNetwork {
    var networkConnected: Boolean = true

    override val isNetworkConnected: Boolean get() = networkConnected
    override fun performRequest(request: HttpRequest<*>): HttpNetworkResponse {
        request.getRequestBody() // we need this so we can test exceptions which occur prior to sending a request
        return (request as MockHttpRequest).mockResponse
    }
}