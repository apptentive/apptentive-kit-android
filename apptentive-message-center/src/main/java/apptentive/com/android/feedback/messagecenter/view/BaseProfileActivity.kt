package apptentive.com.android.feedback.messagecenter.view

import android.app.Activity
import androidx.activity.viewModels
import apptentive.com.android.feedback.Apptentive
import apptentive.com.android.feedback.ApptentiveActivityInfo
import apptentive.com.android.feedback.messagecenter.viewmodel.ProfileViewModel
import apptentive.com.android.ui.ApptentiveViewModelActivity
import apptentive.com.android.ui.ViewModelFactory

/**
 * Base Activity for Edit Profile view
 *
 * This class should be used as base activity for Edit Profile that wish to use
 * full interface customization
 *
 * ApptentiveActivityInfo added for easier integration of future features
 */

open class BaseProfileActivity : ApptentiveViewModelActivity(), ApptentiveActivityInfo {
    /**
     * @property viewModel [ProfileViewModel] class that is responsible for preparing
     * and managing messages for [ProfileActivity]
     *
     */
    val viewModel: ProfileViewModel by viewModels {
        ViewModelFactory { ProfileViewModel() }
    }

    override fun onResume() {
        super.onResume()
        Apptentive.registerApptentiveActivityInfoCallback(this)
    }

    override fun getApptentiveActivityInfo(): Activity {
        return this
    }
}
