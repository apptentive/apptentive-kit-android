package apptentive.com.android.network

import kotlin.math.min

internal class HttpNetworkResponseQueue(private val responses: Array<out HttpNetworkResponse>) {
    private var nextResponseIndex = 0

    fun next(): HttpNetworkResponse {
        val response = responses[nextResponseIndex]
        nextResponseIndex = min(responses.size - 1, nextResponseIndex + 1)
        return response
    }
}
