package apptentive.com.android.network

import java.io.InputStream

/** Interface responsible for reading HTTP-response typed objects. */
interface HttpResponseReader<T> {
    fun read(stream: InputStream): T
}
