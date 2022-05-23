package apptentive.com.android.feedback.messagecenter.view

import androidx.activity.viewModels
import apptentive.com.android.feedback.messagecenter.utils.createMessageCenterViewModel
import apptentive.com.android.feedback.messagecenter.viewmodel.MessageCenterViewModel
import apptentive.com.android.ui.ApptentiveViewModelActivity
import apptentive.com.android.ui.ViewModelFactory

/**
 * Base Activity for Message Center
 *
 * This class should be used as base activity for Message Center that wish to use
 * full interface customization
 */

open class BaseMessageCenterActivity : ApptentiveViewModelActivity() {
    /**
     * @property viewModel [MessageCenterViewModel] class that is responsible for preparing
     * and managing messages for [MessageCenterActivity]
     *
     */
    val viewModel: MessageCenterViewModel by viewModels {
        ViewModelFactory { createMessageCenterViewModel() }
    }
}
