package apptentive.com.android.feedback.model

data class AppRelease(
    val type: String,
    val identifier: String,
    val versionCode: Int = 0,
    val versionName: String,
    val targetSdkVersion: String,
    val debug: Boolean = false,
    val inheritStyle: Boolean = false,
    val overrideStyle: Boolean = false,
    val appStore: String? = null
)