package apptentive.com.android.feedback.model

data class SDK(
    val version: String,
    val platform: String,
    val distribution: String? = null,
    val distributionVersion: String? = null,
    val programmingLanguage: String? = null,
    val authorName: String? = null,
    val authorEmail: String? = null
)
