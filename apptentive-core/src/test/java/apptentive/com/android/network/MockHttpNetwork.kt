package apptentive.com.android.network

import java.io.OutputStream

/**
 * Custom [HttpNetwork] implementation for sync unit testing.
 */
internal class MockHttpNetwork : HttpNetwork {
    var networkConnected: Boolean = true

    override fun isNetworkConnected() = networkConnected

    override fun performRequest(request: HttpRequest<*>): HttpNetworkResponse {
        request.requestBody?.write(NullOutputStream) // we need this to trigger "before send" errors
        return (request.userData as HttpNetworkResponseQueue).next()
    }
}

private object NullOutputStream : OutputStream() {
    override fun write(b: Int) {
    }
}
