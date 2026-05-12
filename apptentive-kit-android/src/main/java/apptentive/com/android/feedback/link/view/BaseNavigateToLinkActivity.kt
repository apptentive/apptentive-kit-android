package apptentive.com.android.feedback.link.view

import androidx.activity.viewModels
import apptentive.com.android.feedback.Apptentive
import apptentive.com.android.feedback.ApptentiveActivityInfo
import apptentive.com.android.feedback.link.NavigateToLinkViewModel
import apptentive.com.android.ui.ApptentiveViewModelActivity
import apptentive.com.android.ui.ViewModelFactory

/**
 * Base Activity for Navigate To Link web view
 *
 * This class should be used as base activity for Navigate to Link that wish to use
 * full interface customization
 *
 * ApptentiveActivityInfo added for easier integration of future features
 */
internal open class BaseNavigateToLinkActivity : ApptentiveViewModelActivity(), ApptentiveActivityInfo {

    val viewModel: NavigateToLinkViewModel by viewModels {
        ViewModelFactory { NavigateToLinkViewModel() }
    }
    override fun getApptentiveActivityInfo() = this

    override fun onResume() {
        super.onResume()
        Apptentive.registerApptentiveActivityInfoCallback(this)
    }
}
