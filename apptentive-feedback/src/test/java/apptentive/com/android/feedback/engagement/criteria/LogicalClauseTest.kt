package apptentive.com.android.feedback.engagement.criteria

import apptentive.com.android.feedback.utils.IndentPrinter
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock

class LogicalClauseTest {

    private val targetingState: TargetingState = mock(TargetingState::class.java)

    private val trueClause = object : Clause {
        override fun evaluate(state: TargetingState, printer: IndentPrinter?): Boolean {
            return true
        }
    }

    private val falseClause = object : Clause {
        override fun evaluate(state: TargetingState, printer: IndentPrinter?): Boolean {
            return false
        }
    }

    @Test
    fun evaluateLogicalAndClauseTest() {
        val passingAndTestCases: List<List<Clause>> = listOf(
            listOf(trueClause),
            listOf(trueClause, trueClause),
            listOf(trueClause, trueClause, trueClause)
        )

        val failingAndTestCases = listOf(
            listOf(falseClause),
            listOf(trueClause, falseClause),
            listOf(falseClause, trueClause),
            listOf(falseClause, falseClause),
            listOf(falseClause, falseClause, trueClause),
            listOf(trueClause, trueClause, falseClause),
        )

        passingAndTestCases.forEach {
            assertTrue(LogicalAndClause(it).evaluate(targetingState, null))
        }

        failingAndTestCases.forEach {
            assertFalse(LogicalAndClause(it).evaluate(targetingState, null))
        }
    }

    @Test
    fun evaluateLogicalOrClauseTest() {
        val passingOrTestCases: List<List<Clause>> = listOf(
            listOf(trueClause),
            listOf(trueClause, trueClause),
            listOf(trueClause, falseClause),
            listOf(falseClause, trueClause),
            listOf(trueClause, trueClause, trueClause),
            listOf(falseClause, trueClause, falseClause),
            listOf(trueClause, falseClause, trueClause),
        )

        val failingOrTestCases = listOf(
            listOf(falseClause),
            listOf(falseClause, falseClause),
            listOf(falseClause, falseClause, falseClause),
        )

        passingOrTestCases.forEach {
            assertTrue(LogicalOrClause(it).evaluate(targetingState, null))
        }

        failingOrTestCases.forEach {
            assertFalse(LogicalOrClause(it).evaluate(targetingState, null))
        }
    }

    @Test
    fun evaluateLogicalNotClauseTest() {
        val passingNotTestCases: List<List<Clause>> = listOf(
            listOf(falseClause)
        )

        val failingNotTestCases = listOf(
            listOf(trueClause)
        )

        passingNotTestCases.forEach {
            assertTrue(LogicalNotClause(it).evaluate(targetingState, null))
        }

        failingNotTestCases.forEach {
            assertFalse(LogicalNotClause(it).evaluate(targetingState, null))
        }
    }

    // Throws exception if more than one child in array
    @Test(expected = IllegalArgumentException::class)
    fun evaluateLogicalNotClauseMoreThanOneItemTest() {
        val failingNotTestCases = listOf(
            listOf(falseClause, falseClause)
        )

        failingNotTestCases.forEach {
            assertFalse(LogicalNotClause(it).evaluate(targetingState, null))
        }
    }
}
