package apptentive.com.android.network

import java.io.OutputStream

interface HttpRequestBody {
    fun write(stream: OutputStream)
}
