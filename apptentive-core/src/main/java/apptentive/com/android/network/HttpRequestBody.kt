package apptentive.com.android.network

import apptentive.com.android.util.InternalUseOnly
import java.io.ByteArrayOutputStream
import java.io.OutputStream

/** Represent an HTTP-request body */
@InternalUseOnly
interface HttpRequestBody {
    /** HTTP-request content type */
    val contentType: String

    /** Writes HTTP-request body to an output stream */
    fun write(stream: OutputStream)
}

@InternalUseOnly
fun HttpRequestBody.asString(): String {
    val stream = ByteArrayOutputStream()
    write(stream)
    return stream.toByteArray().toString(Charsets.UTF_8)
}

internal class BinaryRequestBody(
    private val data: ByteArray,
    override val contentType: String
) : HttpRequestBody {
    override fun write(stream: OutputStream) {
        stream.write(data)
    }
}
