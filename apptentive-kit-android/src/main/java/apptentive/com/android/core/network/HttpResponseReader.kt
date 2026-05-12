package apptentive.com.android.core.network

import apptentive.com.android.core.util.InternalUseOnly

/** Interface responsible for reading HTTP-response typed objects. */
@InternalUseOnly
interface HttpResponseReader<T> {
    fun read(response: HttpNetworkResponse): T
}
