package apptentive.com.android.network

import kotlin.math.min

internal class HttpResponseBodyQueue(private val responses: Array<out HttpResponseBody>) {
    private var nextResponse = 0

    fun next(): HttpResponseBody {
        val response = responses[nextResponse]
        nextResponse = min(responses.size - 1, nextResponse + 1)
        return response
    }
}