package apptentive.com.android.convert

import java.io.InputStream

interface Deserializer {
    fun read(stream: InputStream): Any
}