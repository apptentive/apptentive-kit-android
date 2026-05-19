package apptentive.com.android.feedback

import android.app.Activity
import apptentive.com.android.core.SdkAPI

@SdkAPI
interface ApptentiveActivityInfo {
    fun getApptentiveActivityInfo(): Activity?
}
