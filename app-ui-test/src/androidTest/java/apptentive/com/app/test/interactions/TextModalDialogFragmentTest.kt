package apptentive.com.app.test.interactions

import android.content.Intent
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import apptentive.com.app.test.AbstractActivityTest
import apptentive.com.app.test.MainActivity
import apptentive.com.app.test.R
import apptentive.com.app.test.helpers.checkDoesNotExist
import apptentive.com.app.test.helpers.checkText
import apptentive.com.app.test.helpers.checkVisibility
import apptentive.com.app.test.helpers.clickButton
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TextModalDialogFragmentTest : AbstractActivityTest(createIntent()) {
    @Test
    fun testDismissNote() {
        openNote("simple.json")
        clickButton(R.id.apptentive_note_action_button)
        checkDoesNotExist(R.id.apptentive_note)
    }

    @Test
    fun testSimpleNote() {
        openNote("simple.json")
        checkText(R.id.apptentive_note_action_button, "Dismiss")
    }

    @Test
    fun testMultipleNote() {
        openNote("multiple.json")
        checkText(R.id.apptentive_note_action_button, "Interaction", "Event", "Dismiss")
    }

    @Test
    fun testMultipleStackedNote() {
        openNote("multiple-stacked.json")
        checkText(
            R.id.apptentive_note_action_button,
            "Long Interaction Label",
            "Long Event Label",
            "Long Dismiss Label"
        )
    }

    @Test
    fun testTitleAndMessage() {
        openNote("title-and-body.json")

        checkVisibility(R.id.apptentive_note_title, VISIBLE)
        checkText(R.id.apptentive_note_title, "Title")

        checkVisibility(R.id.apptentive_note_message, VISIBLE)
        checkText(R.id.apptentive_note_message, "Body")
    }

    @Test
    fun testTitleAndNoBody() {
        openNote("title-and-no-body.json")

        checkVisibility(R.id.apptentive_note_title, GONE)
        checkVisibility(R.id.apptentive_note_message, VISIBLE)
        checkText(R.id.apptentive_note_message, "Title")
    }

    @Test
    fun testBodyAndNoTitle() {
        openNote("body-and-no-title.json")

        checkVisibility(R.id.apptentive_note_title, GONE)

        checkVisibility(R.id.apptentive_note_message, VISIBLE)
        checkText(R.id.apptentive_note_message, "Body")
    }

    companion object {
        fun createIntent() =
            Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java).apply {
                putExtra(MainActivity.EXTRA_INTERACTIONS_PATH, "interactions/notes")
            }

        private fun openNote(text: String) {
            clickButton(text)
        }
    }
}