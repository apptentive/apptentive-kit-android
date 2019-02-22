package apptentive.com.android.network

import java.io.InputStream

interface HttpResponseReader<T> {
    fun read(stream: InputStream): T
}
