package apptentive.com.android.convert.json

import org.junit.Test

import org.junit.Assert.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

class JsonSerializationTest {
    @Test
    fun testRoundTrip() {
        val json = """{"value": "test"}"""

        val actual = MySimpleClass()
        actual.value = "test"

        val deserializer = createJsonDeserializer<MySimpleClass>()

        val expected = deserializer.read(openStream(json))
        assertEquals(expected, actual)

        val stream = ByteArrayOutputStream()
        JsonSerializer().write(stream, actual)
        val bytes = stream.toByteArray()

        val restored = deserializer.read(openStream(bytes))
        assertEquals(restored, actual)
    }
}

private fun openStream(value: String): InputStream {
    return openStream(value.toByteArray(Charsets.UTF_8))
}

private fun openStream(bytes: ByteArray): InputStream {
    return ByteArrayInputStream(bytes)
}

private data class MySimpleClass(var value: String? = null)

