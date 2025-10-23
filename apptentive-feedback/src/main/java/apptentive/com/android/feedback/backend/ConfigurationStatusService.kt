package apptentive.com.android.feedback.backend

import apptentive.com.android.feedback.model.SDKConfigurationStatus
import apptentive.com.android.util.Result

internal interface ConfigurationStatusService {
    fun fetchConfigurationStatus(
        conversationToken: String,
        applicationId: String,
        callback: (Result<SDKConfigurationStatus>) -> Unit
    )
}
