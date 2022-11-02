package apptentive.com.android.feedback.utils

import java.io.BufferedOutputStream
import java.io.IOException
import java.io.OutputStream
import kotlin.Throws

/**
 * Counts the number of bytes written to it, and sends them along to the wrapped OutputStream.
 */
class CountingOutputStream(os: OutputStream?) : BufferedOutputStream(os) {
    var bytesWritten = 0L
        private set

    @Throws(IOException::class)
    override fun write(i: Int) {
        bytesWritten++
        super.write(i)
    }

    @Throws(IOException::class)
    override fun write(buffer: ByteArray) {
        bytesWritten += buffer.size
        super.write(buffer)
    }

    @Throws(IOException::class)
    override fun write(buffer: ByteArray, offset: Int, count: Int) {
        bytesWritten += count
        super.write(buffer, offset, count)
    }
}
