package apptentive.com.android.network

import java.io.OutputStream

abstract class HttpRequestBody {
    abstract fun write(stream: OutputStream)
}
