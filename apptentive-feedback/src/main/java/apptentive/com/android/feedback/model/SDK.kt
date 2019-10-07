package apptentive.com.android.feedback.model

data class SDK(
    val version: String,
    val platform: String,
    val distribution: String,
    val distributionVersion: String,
    val programmingLanguage: String? = null,
    val authorName: String? = null,
    val authorEmail: String? = null
)