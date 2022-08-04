package apptentive.com.android.feedback.payload

import com.google.common.truth.Truth.assertThat
import org.junit.Assert
import org.junit.Test

class MediaTypeTest {
    @Test
    fun parseInvalidType() {
        try {
            MediaType.parse("MyMediaType")
            Assert.fail("Should not get there")
        } catch (_: IllegalArgumentException) {
        }

        try {
            MediaType.parse("MyMediaType/MyMediaSubtype/MyMediaBoundary/MyMediaSubSubtype")
            Assert.fail("Should not get there")
        } catch (_: IllegalArgumentException) {
        }
    }

    @Test
    fun parseValidType() {
        val mediaType = MediaType("MyMediaType", "MyMediaSubtype")
        assertThat(mediaType).isEqualTo(MediaType.parse("MyMediaType/MyMediaSubtype"))
    }
}
