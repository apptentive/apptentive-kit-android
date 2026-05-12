package apptentive.com.android.feedback.textmodal

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import apptentive.com.android.core.util.Log
import apptentive.com.android.core.util.LogTags.INTERACTIONS
import apptentive.com.android.feedback.Apptentive
import apptentive.com.android.feedback.ApptentiveActivityInfo

class TextModalSupportFragmentManagerActivity : AppCompatActivity(), ApptentiveActivityInfo {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Apptentive.registerApptentiveActivityInfoCallback(this)
    }

    override fun onResume() {
        super.onResume()
        Log.v(INTERACTIONS, "TextModalSupportFragmentHostActivity launched")
        Apptentive.registerApptentiveActivityInfoCallback(this)
        val noteDialog = TextModalDialogFragment()
        val bundle = Bundle()
        bundle.putBoolean("IS_SDK_HOST_ACTIVITY", true)
        noteDialog.arguments = bundle
        noteDialog.show(supportFragmentManager, TextModalInteraction.TAG)
        Log.d(INTERACTIONS, "Note dialog shown")
    }

    override fun getApptentiveActivityInfo(): Activity {
        return this
    }

    override fun onPause() {
        super.onPause()
        Apptentive.unregisterApptentiveActivityInfoCallback()
    }
}
