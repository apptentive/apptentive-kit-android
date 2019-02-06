package apptentive.com.android.util

import java.io.InputStream
import java.io.OutputStream

object StreamUtils {
    fun readBytes(stream: InputStream, closeOnFinish : Boolean = true) : ByteArray {
        try {
            return stream.readBytes()
        } finally {
            if (closeOnFinish) {
                stream.close()
            }
        }
    }

    fun writeBytes(stream: OutputStream, bytes: ByteArray, closeOnFinish : Boolean = true) {
        try {
            stream.write(bytes)
        } finally {
            if (closeOnFinish) {
                stream.close()
            }
        }
    }
}