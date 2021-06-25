package apptentive.com.android

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule

open class TestCase {
    @get:Rule
    open val dependencyRule = DependencyProviderRule()

    private val results = mutableListOf<Any>()

    //region Before/After

    @Before
    open fun setUp() {
        results.clear()
    }

    //endregion

    //region Results

    protected fun addResult(result: Any) {
        results.add(result)
    }

    protected fun assertResults(vararg expected: Any, clearResults: Boolean = true) {
        assertEquals(expected.toList(), results)
        if (clearResults) {
            results.clear()
        }
    }

    //endregion
}
