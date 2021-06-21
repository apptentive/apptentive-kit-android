package apptentive.com.android.feedback.engagement.criteria

import apptentive.com.android.feedback.engagement.criteria.ConditionalOperator.Companion.AFTER
import apptentive.com.android.feedback.engagement.criteria.ConditionalOperator.Companion.BEFORE
import apptentive.com.android.feedback.engagement.criteria.ConditionalOperator.Companion.CONTAINS
import apptentive.com.android.feedback.engagement.criteria.ConditionalOperator.Companion.ENDS_WITH
import apptentive.com.android.feedback.engagement.criteria.ConditionalOperator.Companion.EQ
import apptentive.com.android.feedback.engagement.criteria.ConditionalOperator.Companion.EXISTS
import apptentive.com.android.feedback.engagement.criteria.ConditionalOperator.Companion.GT
import apptentive.com.android.feedback.engagement.criteria.ConditionalOperator.Companion.GTE
import apptentive.com.android.feedback.engagement.criteria.ConditionalOperator.Companion.LT
import apptentive.com.android.feedback.engagement.criteria.ConditionalOperator.Companion.LTE
import apptentive.com.android.feedback.engagement.criteria.ConditionalOperator.Companion.NE
import apptentive.com.android.feedback.engagement.criteria.ConditionalOperator.Companion.STARTS_WITH
import com.google.common.truth.Truth.assertThat
import org.junit.Ignore
import org.junit.Test

class ConditionalOperatorTest {
    @Test
    fun exists() {
        val op = ConditionalOperator.parse(EXISTS)
        assertThat(op.apply(first = "value", second = true)).isTrue()
        assertThat(op.apply(first = "value", second = false)).isFalse()
        assertThat(op.apply(first = null, second = true)).isFalse()
        assertThat(op.apply(first = null, second = false)).isTrue()
        assertThat(op.apply(first = "value", second = null)).isFalse()
        assertThat(op.apply(first = "value", second = "value")).isFalse()
    }

    @Test
    fun notEqual() {
        val op = ConditionalOperator.parse(NE)
        assertThat(op.apply(first = 10, second = 20)).isTrue()
        assertThat(op.apply(first = 10, second = 10)).isFalse()
        assertThat(op.apply(first = "value", second = "other")).isTrue()
        assertThat(op.apply(first = "value", second = "value")).isFalse()
        assertThat(op.apply(first = "VALUE", second = "value")).isFalse()
        assertThat(op.apply(first = "value", second = "VALUE")).isFalse()
        assertThat(op.apply(first = null, second = "value")).isFalse()
        assertThat(op.apply(first = "value", second = null)).isFalse()
        assertThat(op.apply(first = "value", second = 10)).isFalse()
    }

    @Test
    fun equal() {
        val op = ConditionalOperator.parse(EQ)
        assertThat(op.apply(first = 10, second = 10)).isTrue()
        assertThat(op.apply(first = 10, second = 20)).isFalse()
        assertThat(op.apply(first = "value", second = "value")).isTrue()
        assertThat(op.apply(first = "VALUE", second = "value")).isTrue()
        assertThat(op.apply(first = "value", second = "VALUE")).isTrue()
        assertThat(op.apply(first = null, second = "value")).isFalse()
        assertThat(op.apply(first = "value", second = null)).isFalse()
    }

    @Test
    fun lessThen() {
        val op = ConditionalOperator.parse(LT)
        assertThat(op.apply(first = 10, second = 20)).isTrue()
        assertThat(op.apply(first = 10, second = 10)).isFalse()
        assertThat(op.apply(first = 10, second = 5)).isFalse()
        assertThat(op.apply(first = null, second = 10)).isFalse()
        assertThat(op.apply(first = 10, second = null)).isFalse()
        assertThat(op.apply(first = 10, second = "10")).isFalse()
    }

    @Test
    fun lessThenOrEqual() {
        val op = ConditionalOperator.parse(LTE)
        assertThat(op.apply(first = 10, second = 20)).isTrue()
        assertThat(op.apply(first = 10, second = 10)).isTrue()
        assertThat(op.apply(first = 10, second = 5)).isFalse()
        assertThat(op.apply(first = null, second = 10)).isFalse()
        assertThat(op.apply(first = 10, second = null)).isFalse()
        assertThat(op.apply(first = 10, second = "10")).isFalse()
    }

    @Test
    fun greaterThen() {
        val op = ConditionalOperator.parse(GT)
        assertThat(op.apply(first = 10, second = 20)).isFalse()
        assertThat(op.apply(first = 10, second = 10)).isFalse()
        assertThat(op.apply(first = 10, second = 5)).isTrue()
        assertThat(op.apply(first = null, second = 10)).isFalse()
        assertThat(op.apply(first = 10, second = null)).isFalse()
        assertThat(op.apply(first = 10, second = "10")).isFalse()
    }

    @Test
    fun greaterThanOrEqual() {
        val op = ConditionalOperator.parse(GTE)
        assertThat(op.apply(first = 10, second = 20)).isFalse()
        assertThat(op.apply(first = 10, second = 10)).isTrue()
        assertThat(op.apply(first = 10, second = 5)).isTrue()
        assertThat(op.apply(first = null, second = 10)).isFalse()
        assertThat(op.apply(first = 10, second = null)).isFalse()
        assertThat(op.apply(first = 10, second = "10")).isFalse()
    }

    @Test
    fun contains() {
        val op = ConditionalOperator.parse(CONTAINS)
        assertThat(op.apply(first = "abc", second = "b")).isTrue()
        assertThat(op.apply(first = "abc", second = "B")).isTrue()
        assertThat(op.apply(first = "abc", second = "d")).isFalse()
        assertThat(op.apply(first = "123", second = 123)).isFalse()
        assertThat(op.apply(first = 123, second = "2")).isFalse()
        assertThat(op.apply(first = null, second = "abc")).isFalse()
        assertThat(op.apply(first = "abc", second = null)).isFalse()
        assertThat(op.apply(first = null, second = null)).isFalse()
    }

    @Test
    fun startsWith() {
        val op = ConditionalOperator.parse(STARTS_WITH)
        assertThat(op.apply(first = "abc", second = "a")).isTrue()
        assertThat(op.apply(first = "abc", second = "A")).isTrue()
        assertThat(op.apply(first = "abc", second = "b")).isFalse()
        assertThat(op.apply(first = "123", second = 1)).isFalse()
        assertThat(op.apply(first = 123, second = "1")).isFalse()
        assertThat(op.apply(first = null, second = "abc")).isFalse()
        assertThat(op.apply(first = "abc", second = null)).isFalse()
        assertThat(op.apply(first = null, second = null)).isFalse()
    }

    @Test
    fun endsWith() {
        val op = ConditionalOperator.parse(ENDS_WITH)
        assertThat(op.apply(first = "abc", second = "c")).isTrue()
        assertThat(op.apply(first = "abc", second = "C")).isTrue()
        assertThat(op.apply(first = "abc", second = "b")).isFalse()
        assertThat(op.apply(first = "123", second = 3)).isFalse()
        assertThat(op.apply(first = 123, second = "3")).isFalse()
        assertThat(op.apply(first = null, second = "abc")).isFalse()
        assertThat(op.apply(first = "abc", second = null)).isFalse()
        assertThat(op.apply(first = null, second = null)).isFalse()
    }

    @Test
    @Ignore
    fun beforeDate() {
        val op = ConditionalOperator.parse(BEFORE)
    }

    @Test
    @Ignore
    fun afterDate() {
        val op = ConditionalOperator.parse(AFTER)
    }
}
