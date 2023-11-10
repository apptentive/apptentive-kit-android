package apptentive.com.android.network

import android.os.Build
import apptentive.com.android.util.InternalUseOnly
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.util.Base64

/** Represent an HTTP-request body */
@InternalUseOnly
interface HttpRequestBody {
    /** HTTP-request content type */
    val contentType: String

    /** Writes HTTP-request body to an output stream */
    fun write(stream: OutputStream)

    fun asString(): String {
        val stream = ByteArrayOutputStream()
        write(stream)
        return stream.toByteArray().toString(Charsets.UTF_8)
    }
}

@InternalUseOnly
class BinaryRequestBody(
    private val data: ByteArray,
    override val contentType: String
) : HttpRequestBody {
    override fun write(stream: OutputStream) {
        stream.write(data)
    }

    override fun asString(): String {
        return try {
            when {
                data.size > 5000 -> "Request body too large to print."
                contentType.startsWith("application/json") ||
                    Build.VERSION.SDK_INT < Build.VERSION_CODES.O -> String(
                    data,
                    Charsets.UTF_8
                )

                else -> "Binary data: ${Base64.getEncoder().encodeToString(data)}"
            }
        } catch (e: Exception) {
            "Error while printing request body: ${e.message}"
        }
    }
}
