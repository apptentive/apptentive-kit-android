package apptentive.com.android.network

import java.io.OutputStream

/** Represent an HTTP-request body */
interface HttpRequestBody {
    /** HTTP-request content type */
    val contentType: String

    /** Writes HTTP-request body to an output stream */
    fun write(stream: OutputStream)
}

class BinaryRequestBody(
    private val data: ByteArray,
    override val contentType: String
) : HttpRequestBody {
    override fun write(stream: OutputStream) {
        stream.write(data)
    }
}