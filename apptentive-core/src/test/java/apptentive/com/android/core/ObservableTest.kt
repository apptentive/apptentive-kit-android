package apptentive.com.android.core

import apptentive.com.android.TestCase
import org.junit.Test

import org.junit.Assert.*

class ObservableTest : TestCase() {
    @Test
    fun testValue() {
        val observable = MutableObservable("1")
        // new observer should get current value
        observable.observe {
            addResult("A$it")
        }
        assertResults("A1")

        // new observer should get current value (but others - won't)
        observable.observe {
            addResult("B$it")
        }
        assertResults("B1")

        // new value
        observable.value = "2"
        assertResults("A2", "B2")
    }
}