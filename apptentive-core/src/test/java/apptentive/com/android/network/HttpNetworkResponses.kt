package apptentive.com.android.network

import kotlin.math.min

internal class HttpNetworkResponses(private val responses: Array<out HttpNetworkResponse>) {
    private var nextResponse = 0

    fun next(): HttpNetworkResponse {
        val response = responses[nextResponse]
        nextResponse = min(responses.size - 1, nextResponse + 1)
        return response
    }
}