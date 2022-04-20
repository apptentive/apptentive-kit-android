package apptentive.com.android.feedback.survey

import androidx.activity.viewModels
import apptentive.com.android.feedback.survey.utils.createSurveyViewModel
import apptentive.com.android.feedback.survey.viewmodel.SurveyViewModel
import apptentive.com.android.ui.ApptentiveViewModelActivity
import apptentive.com.android.ui.ViewModelFactory

/**
 * Base Activity for Surveys
 *
 * This class should be used as base activity for Surveys that wish to use
 * full interface customization
 */
open class BaseSurveyActivity : ApptentiveViewModelActivity() {

    /**
     * @property viewModel [SurveyViewModel] class that is responsible for preparing
     * and managing survey data for BaseSurveyActivity
     *
     */
    val viewModel: SurveyViewModel by viewModels {
        ViewModelFactory { createSurveyViewModel() }
    }
}
