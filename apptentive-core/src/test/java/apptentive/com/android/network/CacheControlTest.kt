package apptentive.com.android.network

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CacheControlTest {
    @Test
    fun parse1() {
        val expected = CacheControl(maxAgeSeconds = 18000)
        val actual = CacheControl.parse("max-age=18000")
        assertThat(expected).isEqualTo(actual)
    }

    @Test
    fun parse2() {
        val expected = CacheControl(maxAgeSeconds = 18000)
        val actual = CacheControl.parse("[max-age=18000, private]")
        assertThat(expected).isEqualTo(actual)
    }
}
