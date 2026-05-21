package apptentive.com.android.feedback.interactions.survey

import android.app.Activity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import apptentive.com.android.core.LogTags.SURVEY
import apptentive.com.android.core.MissingProviderException
import apptentive.com.android.feedback.Apptentive
import apptentive.com.android.feedback.ApptentiveActivityInfo
import apptentive.com.android.feedback.interactions.survey.utils.createSurveyViewModel
import apptentive.com.android.feedback.interactions.survey.viewmodel.SurveyViewModel
import apptentive.com.android.ui.ApptentiveViewModelActivity
import apptentive.com.android.ui.ViewModelFactory
import apptentive.com.android.util.Log

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
     * and managing survey data for BaseSurveyActivity.
     */
    lateinit var viewModel: SurveyViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = try {
            ViewModelProvider.create(
                this, ViewModelFactory { createSurveyViewModel() }
            )[SurveyViewModel::class.java]
        } catch (exception: Exception) {
            val logMessage = if (exception is MissingProviderException) {
                "Dependency providers are not registered"
            } else {
                "Error creating SurveyViewModel"
            }
            Log.e(SURVEY, logMessage, exception)
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
