package apptentive.com.android.feedback.survey

import android.app.Activity
import android.os.Bundle
import androidx.activity.viewModels
import apptentive.com.android.feedback.Apptentive
import apptentive.com.android.feedback.ApptentiveActivityInfo
import apptentive.com.android.feedback.survey.utils.createSurveyViewModel
import apptentive.com.android.feedback.survey.viewmodel.SurveyViewModel
import apptentive.com.android.ui.ApptentiveViewModelActivity
import apptentive.com.android.ui.ViewModelFactory

/**
 * Base Activity for Surveys
 *
 * This class should be used as base activity for Surveys that wish to use
 * full interface customization
 *
 * ApptentiveActivityInfo added for easier integration of future features
 */
internal open class BaseSurveyActivity : ApptentiveViewModelActivity(), ApptentiveActivityInfo {

    /**
     * @property viewModel [SurveyViewModel] class that is responsible for preparing
     * and managing survey data for BaseSurveyActivity
     *
     */
    val viewModel: SurveyViewModel by viewModels {
        ViewModelFactory {
            try {
                createSurveyViewModel()
            } catch (exception: Exception) {
                throw IllegalStateException("Issue creating SurveyViewModel $exception")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Calling this in onCreate in case we lose the Activity reference from the last Activity
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
}
