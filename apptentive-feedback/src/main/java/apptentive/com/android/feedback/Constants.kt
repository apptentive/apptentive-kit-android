package apptentive.com.android.feedback

object Constants {
    const val SDK_VERSION = "6.0.0"
    const val API_VERSION = 9
    const val SERVER_URL = "https://api.apptentive.com"
    const val REDACTED_DATA = "<REDACTED>"
    const val CONVERSATION_PATH = "/conversations/:conversation_id/"
    const val PLAY_STORE_URL = "https://play.google.com/store/apps/details?id="

    fun buildHttpPath(path: String): String =
        CONVERSATION_PATH + path
}
