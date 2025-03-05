package apptentive.com.android.feedback.textmodal

import android.app.Activity
import android.os.Bundle
import apptentive.com.android.feedback.Apptentive
import apptentive.com.android.feedback.ApptentiveActivityInfo
import apptentive.com.android.ui.ApptentiveSupportFragmentManagerActivity
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.INTERACTIONS

class TextModalSupportFragmentManagerActivity : ApptentiveSupportFragmentManagerActivity(), ApptentiveActivityInfo {

    override fun onResume() {
        super.onResume()
        Log.d(INTERACTIONS, "ApptentiveSupportFragmentHostActivity launched")
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
}
