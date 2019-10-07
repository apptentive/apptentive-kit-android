package apptentive.com.android.network

/** Interface responsible for reading HTTP-response typed objects. */
interface HttpResponseReader<T> {
    fun read(response: HttpNetworkResponse): T
}
