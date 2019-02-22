package apptentive.com.android.network

import java.io.OutputStream

interface HttpRequestBody {
    val contentType: String;
    fun write(stream: OutputStream)
}
