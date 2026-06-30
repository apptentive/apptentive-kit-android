package apptentive.com.android.feedback.interactions.messagecenter.view

import android.app.Activity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import apptentive.com.android.core.LogTags.MESSAGE_CENTER
import apptentive.com.android.core.MissingProviderException
import apptentive.com.android.feedback.Apptentive
import apptentive.com.android.feedback.ApptentiveActivityInfo
import apptentive.com.android.feedback.interactions.messagecenter.dependencyprovider.createMessageCenterViewModel
import apptentive.com.android.feedback.interactions.messagecenter.viewmodel.MessageCenterViewModel
import apptentive.com.android.ui.ApptentiveViewModelActivity
import apptentive.com.android.ui.ViewModelFactory
import apptentive.com.android.util.Log

/**
 * Base Activity for Message Center
 *
 * This class should be used as base activity for Message Center that wish to use
 * full interface customization
 *
 * [ApptentiveActivityInfo] added for easier integration of future features
 */

internal open class BaseMessageCenterActivity : ApptentiveViewModelActivity(), ApptentiveActivityInfo {
    /**
     * @property viewModel [MessageCenterViewModel] class that is responsible for preparing
     * and managing messages for [MessageCenterActivity].
     */
    internal lateinit var viewModel: MessageCenterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = try {
            ViewModelProvider.create(
                this, ViewModelFactory { createMessageCenterViewModel() }
            )[MessageCenterViewModel::class.java]
        } catch (exception: Exception) {
            val logMessage = if (exception is MissingProviderException) {
                "Dependency providers are not registered"
            } else {
                "Error creating MessageCenterViewModel"
            }
            Log.e(MESSAGE_CENTER, logMessage, exception)
            finish()
            return
        }
        // Calling this in onCreate in case we lose the Activity reference from the last Activity,
        // and before we can register in onResume.
        Apptentive.registerApptentiveActivityInfoCallback(this)
    }

    override fun onResume() {
        super.onResume()
        Apptentive.registerApptentiveActivityInfoCallback(this)
    }

    override fun getApptentiveActivityInfo(): Activity {
        return this
    }

    fun isViewModelInitialized() = ::viewModel.isInitialized
}
