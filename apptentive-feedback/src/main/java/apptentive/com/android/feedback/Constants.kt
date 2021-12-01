package apptentive.com.android.feedback

object Constants {
    const val SDK_VERSION = "6.0.0"
    const val API_VERSION = 11
    const val SERVER_URL = "https://api.apptentive.com"
    const val REDACTED_DATA = "<REDACTED>"
    const val CONVERSATION_PATH = "/conversations/:conversation_id/"
    const val SHARED_PREF_CUSTOM_STORE_URL = "com.apptentive.sdk.customstoreurl"
    const val SHARED_PREF_CUSTOM_STORE_URL_KEY = "custom_store_url_key"

    fun buildHttpPath(path: String): String =
        CONVERSATION_PATH + path
}
