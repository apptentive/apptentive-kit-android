package apptentive.com.app.test.helpers

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText

fun checkText(id: Int, text: String) {
    onView(withId(id)).check(matches(withText(text)))
}

fun checkText(id: Int, vararg texts: String) {
    texts.forEachIndexed { index, text ->
        onView(withIndex(withId(id), index)).check(matches(withText(text)))
    }
}