package apptentive.com.android.network

import android.os.Build
import androidx.annotation.RequiresApi
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

    fun asString(): String
}

@InternalUseOnly

internal class BinaryRequestBody(
    private val data: ByteArray,
    override val contentType: String
) : HttpRequestBody {
    override fun write(stream: OutputStream) {
        stream.write(data)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun asString(): String {
        val stream = ByteArrayOutputStream()
        write(stream)

        return Base64.getEncoder().encodeToString(stream.toByteArray())
    }
}
