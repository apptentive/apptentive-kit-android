package apptentive.com.app.test.helpers

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText

fun clickButton(id: Int) {
    onView(withId(id)).perform(click())
}

fun clickButton(text: String) {
    onView(withText(text)).perform(click())
}