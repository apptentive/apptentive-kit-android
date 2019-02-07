package apptentive.com.android.util

import java.io.InputStream
import java.io.OutputStream

/**
 * Helper singleton class to hide/simplify some of the higher order functions (code becomes more readable)
 * */
object StreamUtils {
    /**
     * Reads bytes from the [stream] and then closes it down correctly whether an exception is thrown or not.
     */
    fun readAndClose(stream: InputStream): ByteArray {
        stream.use { s ->
            return s.readBytes()
        }
    }

    /**
     * Writes bytes to the [stream] and then closes it down correctly whether an exception is thrown or not.
     */
    fun writeAndClose(stream: OutputStream, bytes: ByteArray) {
        stream.use { s ->
            s.write(bytes)
        }
    }
}