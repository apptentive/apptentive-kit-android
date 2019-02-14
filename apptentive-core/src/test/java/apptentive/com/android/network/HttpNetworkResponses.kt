package apptentive.com.android.network

internal class HttpNetworkResponses(private val responses: Array<HttpNetworkResponse>) {
    private var nextResponse = 0

    fun hasNext(): Boolean {
        return nextResponse < responses.size
    }

    fun next(): HttpNetworkResponse {
        return responses[++nextResponse]
    }
}