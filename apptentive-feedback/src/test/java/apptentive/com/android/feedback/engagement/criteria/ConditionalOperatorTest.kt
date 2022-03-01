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
import apptentive.com.android.feedback.engagement.interactions.InteractionResponse
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Test

class ConditionalOperatorTest {
    private val responsesId = setOf(InteractionResponse.IdResponse("aaa111"))
    private val responsesLong = setOf(InteractionResponse.LongResponse(5))
    private val responsesString = setOf(InteractionResponse.StringResponse("123"))
    private val responsesOther = setOf(InteractionResponse.OtherResponse("cba", "321"))
    private val responsesGroup = setOf(
        InteractionResponse.IdResponse("aaa111"),
        InteractionResponse.IdResponse("bbb222"),
        InteractionResponse.OtherResponse("ccc", "333")
    )

    @Test
    fun exists() {
        val op = ConditionalOperator.parse(EXISTS)
        assertTrue(op.apply(first = "value", second = true))
        assertFalse(op.apply(first = "value", second = false))
        assertFalse(op.apply(first = null, second = true))
        assertTrue(op.apply(first = null, second = false))
        assertFalse(op.apply(first = "value", second = null))
        assertFalse(op.apply(first = "value", second = "value"))
        assertTrue(op.apply(first = responsesId, second = true))
        assertTrue(op.apply(first = responsesLong, second = true))
        assertTrue(op.apply(first = responsesString, second = true))
        assertTrue(op.apply(first = responsesOther, second = true))
        assertTrue(op.apply(first = responsesGroup, second = true))
    }

    @Test
    fun notEqual() {
        val op = ConditionalOperator.parse(NE)
        assertTrue(op.apply(first = 10, second = 20))
        assertFalse(op.apply(first = 10, second = 10))
        assertTrue(op.apply(first = "value", second = "other"))
        assertFalse(op.apply(first = "value", second = "value"))
        assertFalse(op.apply(first = "VALUE", second = "value"))
        assertFalse(op.apply(first = "value", second = "VALUE"))
        assertFalse(op.apply(first = null, second = "value"))
        assertFalse(op.apply(first = "value", second = null))
        assertFalse(op.apply(first = "value", second = 10))
        assertFalse(op.apply(first = responsesId, second = "aaa111"))
        assertFalse(op.apply(first = responsesLong, second = 5.0))
        assertFalse(op.apply(first = responsesString, second = "123"))
        assertFalse(op.apply(first = responsesOther, second = "321"))
        assertFalse(op.apply(first = responsesOther, second = "cba"))
        assertFalse(op.apply(first = responsesGroup, second = "aaa111"))
        assertFalse(op.apply(first = responsesGroup, second = "bbb222"))
        assertFalse(op.apply(first = responsesGroup, second = "ccc"))
        assertFalse(op.apply(first = responsesGroup, second = "333"))
        assertTrue(op.apply(first = setOf<InteractionResponse>(), second = "aaa111"))
        assertTrue(op.apply(first = setOf<InteractionResponse>(), second = "aa11"))
        assertTrue(op.apply(first = setOf<InteractionResponse>(), second = 5.0))
        assertTrue(op.apply(first = setOf<InteractionResponse>(), second = 4.0))
        assertTrue(op.apply(first = responsesId, second = "aa11"))
        assertTrue(op.apply(first = responsesLong, second = 4.0))
        assertTrue(op.apply(first = responsesString, second = "321"))
        assertTrue(op.apply(first = responsesOther, second = "bca"))
        assertTrue(op.apply(first = responsesGroup, second = "aa11"))
        assertTrue(op.apply(first = responsesGroup, second = "bb22"))
        assertTrue(op.apply(first = responsesGroup, second = "cc"))
    }

    @Test
    fun equal() {
        val op = ConditionalOperator.parse(EQ)
        assertTrue(op.apply(first = 10, second = 10))
        assertFalse(op.apply(first = 10, second = 20))
        assertTrue(op.apply(first = "value", second = "value"))
        assertTrue(op.apply(first = "VALUE", second = "value"))
        assertTrue(op.apply(first = "value", second = "VALUE"))
        assertFalse(op.apply(first = null, second = "value"))
        assertFalse(op.apply(first = "value", second = null))
        assertTrue(op.apply(first = responsesId, second = "aaa111"))
        assertTrue(op.apply(first = responsesLong, second = 5.0))
        assertTrue(op.apply(first = responsesString, second = "123"))
        assertTrue(op.apply(first = responsesOther, second = "cba"))
        assertTrue(op.apply(first = responsesOther, second = "321"))
        assertTrue(op.apply(first = responsesGroup, second = "aaa111"))
        assertTrue(op.apply(first = responsesGroup, second = "bbb222"))
        assertTrue(op.apply(first = responsesGroup, second = "333"))
        assertTrue(op.apply(first = responsesGroup, second = "ccc"))
        assertFalse(op.apply(first = setOf<InteractionResponse>(), second = "aaa111"))
        assertFalse(op.apply(first = setOf<InteractionResponse>(), second = "aa11"))
        assertFalse(op.apply(first = setOf<InteractionResponse>(), second = 5.0))
        assertFalse(op.apply(first = setOf<InteractionResponse>(), second = 4.0))
        assertFalse(op.apply(first = responsesId, second = "aa11"))
        assertFalse(op.apply(first = responsesLong, second = 6.0))
        assertFalse(op.apply(first = responsesString, second = "321"))
        assertFalse(op.apply(first = responsesOther, second = "abc"))
        assertFalse(op.apply(first = responsesOther, second = "231"))
        assertFalse(op.apply(first = responsesGroup, second = "aa11"))
        assertFalse(op.apply(first = responsesGroup, second = "bb22"))
        assertFalse(op.apply(first = responsesGroup, second = "cc"))
    }

    @Test
    fun lessThen() {
        val op = ConditionalOperator.parse(LT)
        assertTrue(op.apply(first = 10, second = 20))
        assertFalse(op.apply(first = 10, second = 10))
        assertFalse(op.apply(first = 10, second = 5))
        assertFalse(op.apply(first = null, second = 10))
        assertFalse(op.apply(first = 10, second = null))
        assertFalse(op.apply(first = 10, second = "10"))
        assertTrue(op.apply(first = responsesLong, second = 6.0))
        assertFalse(op.apply(first = responsesLong, second = 5.0))
        assertFalse(op.apply(first = responsesLong, second = 4.0))
    }

    @Test
    fun lessThenOrEqual() {
        val op = ConditionalOperator.parse(LTE)
        assertTrue(op.apply(first = 10, second = 20))
        assertTrue(op.apply(first = 10, second = 10))
        assertFalse(op.apply(first = 10, second = 5))
        assertFalse(op.apply(first = null, second = 10))
        assertFalse(op.apply(first = 10, second = null))
        assertFalse(op.apply(first = 10, second = "10"))
        assertTrue(op.apply(first = responsesLong, second = 6.0))
        assertTrue(op.apply(first = responsesLong, second = 5.0))
        assertFalse(op.apply(first = responsesLong, second = 4.0))
    }

    @Test
    fun greaterThen() {
        val op = ConditionalOperator.parse(GT)
        assertFalse(op.apply(first = 10, second = 20))
        assertFalse(op.apply(first = 10, second = 10))
        assertTrue(op.apply(first = 10, second = 5))
        assertFalse(op.apply(first = null, second = 10))
        assertFalse(op.apply(first = 10, second = null))
        assertFalse(op.apply(first = 10, second = "10"))
        assertTrue(op.apply(first = responsesLong, second = 4.0))
        assertFalse(op.apply(first = responsesLong, second = 5.0))
        assertFalse(op.apply(first = responsesLong, second = 6.0))
    }

    @Test
    fun greaterThanOrEqual() {
        val op = ConditionalOperator.parse(GTE)
        assertFalse(op.apply(first = 10, second = 20))
        assertTrue(op.apply(first = 10, second = 10))
        assertTrue(op.apply(first = 10, second = 5))
        assertFalse(op.apply(first = null, second = 10))
        assertFalse(op.apply(first = 10, second = null))
        assertFalse(op.apply(first = 10, second = "10"))
        assertTrue(op.apply(first = responsesLong, second = 4.0))
        assertTrue(op.apply(first = responsesLong, second = 5.0))
        assertFalse(op.apply(first = responsesLong, second = 6.0))
    }

    @Test
    fun contains() {
        val op = ConditionalOperator.parse(CONTAINS)
        assertTrue(op.apply(first = "abc", second = "b"))
        assertTrue(op.apply(first = "abc", second = "B"))
        assertFalse(op.apply(first = "abc", second = "d"))
        assertFalse(op.apply(first = "123", second = 123))
        assertFalse(op.apply(first = 123, second = "2"))
        assertFalse(op.apply(first = null, second = "abc"))
        assertFalse(op.apply(first = "abc", second = null))
        assertFalse(op.apply(first = null, second = null))
        assertTrue(op.apply(first = responsesString, second = "123"))
        assertTrue(op.apply(first = responsesString, second = "2"))
        assertTrue(op.apply(first = responsesOther, second = "32"))
        assertFalse(op.apply(first = responsesString, second = "13"))
        assertFalse(op.apply(first = responsesOther, second = "ab"))
        assertFalse(op.apply(first = responsesOther, second = "12"))
    }

    @Test
    fun startsWith() {
        val op = ConditionalOperator.parse(STARTS_WITH)
        assertTrue(op.apply(first = "abc", second = "a"))
        assertTrue(op.apply(first = "abc", second = "A"))
        assertFalse(op.apply(first = "abc", second = "b"))
        assertFalse(op.apply(first = "123", second = 1))
        assertFalse(op.apply(first = 123, second = "1"))
        assertFalse(op.apply(first = null, second = "abc"))
        assertFalse(op.apply(first = "abc", second = null))
        assertFalse(op.apply(first = null, second = null))
        assertTrue(op.apply(first = responsesString, second = "12"))
        assertTrue(op.apply(first = responsesOther, second = "32"))
        assertFalse(op.apply(first = responsesString, second = "23"))
        assertFalse(op.apply(first = responsesOther, second = "c"))
        assertFalse(op.apply(first = responsesOther, second = "2"))
    }

    @Test
    fun endsWith() {
        val op = ConditionalOperator.parse(ENDS_WITH)
        assertTrue(op.apply(first = "abc", second = "c"))
        assertTrue(op.apply(first = "abc", second = "C"))
        assertFalse(op.apply(first = "abc", second = "b"))
        assertFalse(op.apply(first = "123", second = 3))
        assertFalse(op.apply(first = 123, second = "3"))
        assertFalse(op.apply(first = null, second = "abc"))
        assertFalse(op.apply(first = "abc", second = null))
        assertFalse(op.apply(first = null, second = null))
        assertTrue(op.apply(first = responsesString, second = "23"))
        assertTrue(op.apply(first = responsesOther, second = "21"))
        assertFalse(op.apply(first = responsesString, second = "12"))
        assertFalse(op.apply(first = responsesOther, second = "c"))
        assertFalse(op.apply(first = responsesOther, second = "3"))
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
