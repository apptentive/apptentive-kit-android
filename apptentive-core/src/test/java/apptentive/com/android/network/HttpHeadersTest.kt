package apptentive.com.android.network

import apptentive.com.android.network.HttpHeaders.Companion.acceptEncodingHeader
import apptentive.com.android.network.HttpHeaders.Companion.contentLengthHeader
import org.junit.Test

import org.junit.Assert.*

class HttpHeadersTest {
    @Test
    fun setSetValues() {
        val headers = HttpHeaders()
        headers[acceptEncodingHeader] = "application/json"
        headers[contentLengthHeader] = "1000"
        for (entry in headers.entries) {
            when (entry.key) {
                acceptEncodingHeader -> assertEquals(entry.value, "application/json")
                contentLengthHeader -> assertEquals(entry.value, "1000")
            }
        }

    }
}