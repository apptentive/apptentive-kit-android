package apptentive.com.android.feedback.model

data class IntegrationConfig(
    val apptentive: IntegrationConfigItem? = null,
    val amazonAwsSns: IntegrationConfigItem? = null,
    val urbanAirship: IntegrationConfigItem? = null,
    val parse: IntegrationConfigItem? = null
)
