package apptentive.com.android.feedback.enjoyment

import android.app.Activity
import android.os.Bundle
import apptentive.com.android.feedback.Apptentive
import apptentive.com.android.feedback.ApptentiveActivityInfo
import apptentive.com.android.ui.ApptentiveSupportFragmentManagerActivity
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.INTERACTIONS

class EnjoymentDialogSupportFragmentManagerActivity : ApptentiveSupportFragmentManagerActivity(), ApptentiveActivityInfo {
    override fun getApptentiveActivityInfo(): Activity {
        return this
    }

    override fun onResume() {
        super.onResume()
        Log.d(INTERACTIONS, "ApptentiveSupportFragmentHostActivity launched")
        Apptentive.registerApptentiveActivityInfoCallback(this)

        val loveDialog = EnjoymentDialogFragment()
        val bundle = Bundle()
        bundle.putBoolean("IS_SDK_HOST_ACTIVITY", true)
        loveDialog.arguments = bundle
        loveDialog.show(supportFragmentManager, EnjoymentDialogInteraction.TAG)
    }
}
