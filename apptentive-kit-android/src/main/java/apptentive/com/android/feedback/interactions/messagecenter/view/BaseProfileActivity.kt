package apptentive.com.android.feedback.interactions.messagecenter.view

import android.app.Activity
import android.os.Bundle
import androidx.activity.viewModels
import apptentive.com.android.feedback.Apptentive
import apptentive.com.android.feedback.ApptentiveActivityInfo
import apptentive.com.android.feedback.interactions.messagecenter.viewmodel.ProfileViewModel
import apptentive.com.android.ui.ApptentiveViewModelActivity
import apptentive.com.android.ui.ViewModelFactory
import kotlin.getValue

/**
 * Base Activity for Edit Profile view
 *
 * This class should be used as base activity for Edit Profile that wish to use
 * full interface customization
 *
 * ApptentiveActivityInfo added for easier integration of future features
 */

internal open class BaseProfileActivity : ApptentiveViewModelActivity(), ApptentiveActivityInfo {
    /**
     * @property viewModel [ProfileViewModel] class that is responsible for preparing
     * and managing messages for [ProfileActivity]
     *
     */
    val viewModel: ProfileViewModel by viewModels {
        ViewModelFactory { ProfileViewModel() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.dismissActivity.observe(this) {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        Apptentive.registerApptentiveActivityInfoCallback(this)
    }

    override fun getApptentiveActivityInfo(): Activity {
        return this
    }
}
