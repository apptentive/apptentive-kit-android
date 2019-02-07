package apptentive.com.android.network

import apptentive.com.android.network.HttpHeaders.Companion.acceptEncodingHeader
import apptentive.com.android.network.HttpHeaders.Companion.contentLengthHeader
import org.junit.Assert.assertEquals
import org.junit.Test

class HttpHeadersTest {
    @Test
    fun setSetValues() {
        val headers = MutableHttpHeaders()
        headers[acceptEncodingHeader] = "application/json"
        headers[contentLengthHeader] = "1000"
        for (entry in headers) {
            when (entry.key) {
                acceptEncodingHeader -> assertEquals(entry.value, "application/json")
                contentLengthHeader -> assertEquals(entry.value, "1000")
            }
        }

    }
}