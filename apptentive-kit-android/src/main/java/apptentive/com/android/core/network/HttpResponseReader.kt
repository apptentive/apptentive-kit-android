package apptentive.com.android.core.network

/** Interface responsible for reading HTTP-response typed objects. */
internal interface HttpResponseReader<T> {
    fun read(response: HttpNetworkResponse): T
}
