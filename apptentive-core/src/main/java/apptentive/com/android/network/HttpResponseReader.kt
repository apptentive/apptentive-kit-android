package apptentive.com.android.network

import apptentive.com.android.util.InternalUseOnly

/** Interface responsible for reading HTTP-response typed objects. */
@InternalUseOnly
interface HttpResponseReader<T> {
    fun read(response: HttpNetworkResponse): T
}
