package apptentive.com.android.feedback.utils

import apptentive.com.android.TestCase
import org.junit.Test
import java.io.ByteArrayInputStream
import com.google.common.truth.Truth.assertThat

class MultipartParserTest : TestCase() {
    @Test
    fun testParsing() {
        val haystack = "--ybjh537zxrdma2e8ngpc9v4fkolqtwi0u61s\r\nContent-Disposition: form-data; name=\"message\"\r\nContent-Type: application/json;charset=UTF-8\r\n\r\n{\"session_id\":\"2A544CD9-CAAF-4FED-B732-B3101330C088\",\"hidden\":false,\"automated\":false,\"nonce\":\"1CC1B998-A525-49FA-8DE2-3C393018069C\",\"custom_data\":null,\"client_created_at_utc_offset\":-25200,\"client_created_at\":1696619726.151576,\"body\":\"Test\"}\r\n--ybjh537zxrdma2e8ngpc9v4fkolqtwi0u61s\r\nContent-Disposition: form-data; name=\"file[]\"; filename=\"IMG_0005.jpeg\"\r\nContent-Type: image/jpeg\r\n\r\nfjdakl;jklfdsajkl;fdasjklf;adsjkhagihwehpikl;hk;lfsjiopvwiohpewekasfkj;fasdhioasdhoipfaseohjifsadkjfasd\r\n--ybjh537zxrdma2e8ngpc9v4fkolqtwi0u61s--\r\n"
        val inputStream = ByteArrayInputStream(haystack.toByteArray())
        val boundary = "ybjh537zxrdma2e8ngpc9v4fkolqtwi0u61s"

        val parser = MultipartParser(inputStream, boundary)

        assertThat(parser.numberOfParts).isEqualTo(2)

        val firstPart = parser.getPartAtIndex(0)
        assertThat(firstPart).isNotNull()
        assertThat(firstPart!!.headers).isEqualTo("Content-Disposition: form-data; name=\"message\"\r\n" +
                "Content-Type: application/json;charset=UTF-8")
        assertThat(String(firstPart!!.content, Charsets.UTF_8)).isEqualTo("{\"session_id\":\"2A544CD9-CAAF-4FED-B732-B3101330C088\",\"hidden\":false,\"automated\":false,\"nonce\":\"1CC1B998-A525-49FA-8DE2-3C393018069C\",\"custom_data\":null,\"client_created_at_utc_offset\":-25200,\"client_created_at\":1696619726.151576,\"body\":\"Test\"}")

        val secondPart = parser.getPartAtIndex(1)
        assertThat(secondPart).isNotNull()
        assertThat(secondPart!!.headers).isEqualTo("Content-Disposition: form-data; name=\"file[]\"; filename=\"IMG_0005.jpeg\"\r\n" +
                "Content-Type: image/jpeg")
        assertThat(secondPart.content).isEqualTo("fjdakl;jklfdsajkl;fdasjklf;adsjkhagihwehpikl;hk;lfsjiopvwiohpewekasfkj;fasdhioasdhoipfaseohjifsadkjfasd".toByteArray())
    }
}