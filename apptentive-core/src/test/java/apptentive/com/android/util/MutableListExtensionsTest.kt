package apptentive.com.android.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MutableListExtensionsTest {
    @Test
    fun testRemove() {
        val list = mutableListOf(1, 2, 3)
        val item = list.remove { it == 2 }
        assertThat(2 as Int).isEqualTo(item)

        val expected = listOf(1, 3)
        assertThat(expected).isEqualTo(list)
    }

    @Test
    fun testRemoveMissing() {
        val list = mutableListOf(1, 2, 3)
        val item = list.remove { it == 4 }
        assertThat(item).isNull()

        val expected = listOf(1, 2, 3)
        assertThat(expected).isEqualTo(list)
    }
}
