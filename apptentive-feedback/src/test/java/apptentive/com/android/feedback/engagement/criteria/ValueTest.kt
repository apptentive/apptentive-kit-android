package apptentive.com.android.feedback.engagement.criteria

import com.google.common.truth.Truth.assertThat
import org.junit.Ignore
import org.junit.Test

typealias string = Value.String
typealias number = Value.Number

class ValueTest {
    // region Boolean

    @Test
    @Ignore
    fun booleanEquality() {

    }

    @Test
    @Ignore
    fun booleanComparable() {
    }

    //endregion

    //region String

    @Test
    fun stringEquality() {
        assertThat(string("value")).isEqualTo(string("value"))
        assertThat(string("value")).isEqualTo(string("VALUE"))
        assertThat(string("VALUE")).isEqualTo(string("value"))

        assertThat(string("value")).isNotEqualTo(string("another value"))
    }

    @Test
    fun stringComparable() {
        assertThat(string("A")).isLessThan(string("B"))
        assertThat(string("B")).isGreaterThan(string("A"))
        assertThat(string("C")).isEquivalentAccordingToCompareTo(string("C"))
    }

    @Test
    fun stringStartsWith() {
        assertThat(string("abc").startsWith(string("ab"))).isTrue()
        assertThat(string("ABC").startsWith(string("ab"))).isTrue()
        assertThat(string("abc").startsWith(string("AB"))).isTrue()

        assertThat(string("abc").startsWith(string("c"))).isFalse()
    }

    @Test
    fun stringEndsWith() {
        assertThat(string("abc").endsWith(string("bc"))).isTrue()
        assertThat(string("ABC").endsWith(string("bc"))).isTrue()
        assertThat(string("abc").endsWith(string("BC"))).isTrue()

        assertThat(string("abc").endsWith(string("a"))).isFalse()
    }

    @Test
    fun stringContains() {
        assertThat(string("abc").contains(string("b"))).isTrue()
        assertThat(string("ABC").contains(string("b"))).isTrue()
        assertThat(string("abc").contains(string("B"))).isTrue()

        assertThat(string("abc").contains(string("d"))).isFalse()
    }

    //endregion

    //region Number

    @Test
    fun numberEquality() {
        assertThat(number(1570831279840)).isEqualTo(number(1570831279840))
    }

    @Test
    @Ignore
    fun numberComparable() {
    }

    //endregion

    //region DateTime

    @Test
    @Ignore
    fun dateTimeEquality() {

    }

    @Test
    @Ignore
    fun dateTimeComparable() {
    }

    //endregion

    //region Version

    @Test
    @Ignore
    fun versionEquality() {

    }

    @Test
    @Ignore
    fun versionComparable() {
    }

    //endregion
}