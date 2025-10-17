package apptentive.com.android.feedback.backend

import apptentive.com.android.feedback.model.Configuration
import apptentive.com.android.util.Result

internal interface ConfigurationStatusService {
    fun fetchConfigurationStatus(
        conversationToken: String,
        applicationId: String,
        callback: (Result<Configuration>) -> Unit
    )
}
