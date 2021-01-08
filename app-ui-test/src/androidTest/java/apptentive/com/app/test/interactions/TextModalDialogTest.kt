package apptentive.com.app.test.interactions

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import apptentive.com.app.test.AbstractActivityTest
import apptentive.com.app.test.MainActivity
import apptentive.com.app.test.R
import apptentive.com.app.test.helpers.checkText
import apptentive.com.app.test.helpers.clickButton
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TextModalDialogTest : AbstractActivityTest(createIntent()) {
    @Test
    fun testSimpleNote() {
        clickButton("simple.json")
        checkText(R.id.apptentive_note_title, "Title")
        checkText(R.id.apptentive_note_action_button, "Dismiss")
    }

    companion object {
        fun createIntent() =
            Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java).apply {
                putExtra(MainActivity.EXTRA_INTERACTIONS_PATH, "interactions/notes")
            }
    }
}