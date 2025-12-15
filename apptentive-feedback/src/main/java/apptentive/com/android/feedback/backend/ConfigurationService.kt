package apptentive.com.android.feedback.backend

import apptentive.com.android.feedback.model.Configuration
import apptentive.com.android.util.Result

internal interface ConfigurationService {
    fun fetchConfiguration(
        conversationToken: String,
        conversationId: String,
        callback: (Result<Configuration>) -> Unit
    )
}
