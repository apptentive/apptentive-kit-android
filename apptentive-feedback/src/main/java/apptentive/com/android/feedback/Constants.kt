package apptentive.com.android.feedback

import apptentive.com.android.util.InternalUseOnly

@InternalUseOnly
object Constants {
    const val SDK_VERSION = "6.10.0"
    const val API_VERSION = 15
    const val SERVER_URL = "https://api.apptentive.com"
    const val AU_SERVER_URL = "https://web-api-k8s.digital.alchemer-au.com"
    const val EU_SERVER_URL = "https://web-api-k8s.digital.alchemer.eu"
    const val STAGING1 = "https://web-api.staging.apptentive.com"
    const val STAGING2 = "https://api-k8s-identity.staging.apptentive.com/"
    const val REDACTED_DATA = "<REDACTED>"
    private const val CONVERSATION_PATH = "/conversations/:conversation_id/"

    fun buildHttpPath(path: String): String =
        CONVERSATION_PATH + path
}
