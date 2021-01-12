package apptentive.com.app.test.helpers

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.matcher.ViewMatchers.withId

fun checkDoesNotExist(id: Int) {
    onView(withId(id)).check(doesNotExist())
}