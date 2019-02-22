package apptentive.com.android.convert

import java.io.OutputStream

interface Serializer {
    fun write(stream: OutputStream, target: Any)
}