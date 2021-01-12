package apptentive.com.app.test.helpers

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*

fun checkVisibility(id: Int, visibility: Int) {
    onView(withId(id)).check(matches(withEffectiveVisibility(getVisibility(visibility))))
}

private fun getVisibility(visibility: Int) = when (visibility) {
    View.VISIBLE -> Visibility.VISIBLE
    View.INVISIBLE -> Visibility.INVISIBLE
    View.GONE -> Visibility.GONE
    else -> throw IllegalArgumentException("Invalid visibility value: $visibility")
}
