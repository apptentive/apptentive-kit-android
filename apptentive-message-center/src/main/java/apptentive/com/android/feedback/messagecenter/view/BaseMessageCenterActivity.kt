package apptentive.com.android.feedback.messagecenter.view

import android.app.Activity
import androidx.activity.viewModels
import apptentive.com.android.feedback.Apptentive
import apptentive.com.android.feedback.ApptentiveActivityInfo
import apptentive.com.android.feedback.dependencyprovider.createMessageCenterViewModel
import apptentive.com.android.feedback.messagecenter.viewmodel.MessageCenterViewModel
import apptentive.com.android.ui.ApptentiveViewModelActivity
import apptentive.com.android.ui.ViewModelFactory

/**
 * Base Activity for Message Center
 *
 * This class should be used as base activity for Message Center that wish to use
 * full interface customization
 *
 * ApptentiveActivityInfo added for easier integration of future features
 */

open class BaseMessageCenterActivity : ApptentiveViewModelActivity(), ApptentiveActivityInfo {
    /**
     * @property viewModel [MessageCenterViewModel] class that is responsible for preparing
     * and managing messages for [MessageCenterActivity]
     *
     */
    val viewModel: MessageCenterViewModel by viewModels {
        ViewModelFactory { createMessageCenterViewModel() }
    }

    override fun onResume() {
        super.onResume()
        Apptentive.registerApptentiveActivityInfoCallback(this)
    }

    override fun getApptentiveActivityInfo(): Activity {
        return this
    }

    override fun onPause() {
        Apptentive.unregisterApptentiveActivityInfoCallback()
        super.onPause()
    }
}
