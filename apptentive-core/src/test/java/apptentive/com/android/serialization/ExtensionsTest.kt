package apptentive.com.android.serialization

import apptentive.com.android.util.decodeFromByteArray
import apptentive.com.android.util.encodeToByteArray
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ExtensionsTest {
    @Test
    fun encodeNullableString() {
        val s1 = "This is string"
        val s2: String? = null
        val s3 = "別の文字列"
        val bytes = encodeToByteArray { out ->
            out.encodeNullableString(s1)
            out.encodeNullableString(s2)
            out.encodeNullableString(s3)
        }
        decodeFromByteArray(bytes) { input ->
            assertThat(s1).isEqualTo(input.decodeNullableString())
            assertThat(input.decodeNullableString()).isNull()
            assertThat(s3).isEqualTo(input.decodeNullableString())
        }
    }

    @Test
    fun encodeEnum() {
        val s1 = TestEnum.One
        val s2 = TestEnum.Two
        val bytes = encodeToByteArray { out ->
            out.encodeEnum(s1)
            out.encodeEnum(s2)
        }
        decodeFromByteArray(bytes) { input ->
            assertThat(s1).isEqualTo(input.decodeEnum<TestEnum>())
            assertThat(s2).isEqualTo(input.decodeEnum<TestEnum>())
        }
    }

    @Test
    fun encodeMap() {
        val expected = mapOf(
            "key1" to true,
            "key2" to 10.toByte(),
            "key3" to 20.toShort(),
            "key4" to 30,
            "key5" to 40.toLong(),
            "key6" to 3.14f,
            "key7" to 4.14,
            "key8" to "Strøng",
            "key9" to null
        )
        val bytes = encodeToByteArray { out ->
            out.encodeMap(expected)
        }
        val actual = decodeFromByteArray(bytes) { input ->
            input.decodeMap()
        }
        assertThat(expected).isEqualTo(actual)
    }

    private enum class TestEnum {
        One, Two, Three
    }
}
