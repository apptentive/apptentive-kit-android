package apptentive.com.android.platform

import apptentive.com.android.util.InternalUseOnly

@InternalUseOnly
object SharedPrefConstants {
    const val APPTENTIVE = "APPTENTIVE"
    const val INTERACTION_BACKUP = "interaction_backup"

    const val REGISTRATION_INFO = "com.apptentive.sdk.registrationinfo"
    const val APPTENTIVE_KEY_HASH = "apptentive_key_hash"
    const val APPTENTIVE_SIGNATURE_HASH = "apptentive_signature_hash"

    const val POST_NOTIFICATIONS = "android.permission.POST_NOTIFICATIONS"
    const val PREF_KEY_PUSH_PROVIDER = "pushProvider"
    const val PREF_KEY_PUSH_TOKEN = "pushToken"

    const val USE_HOST_APP_THEME = "com.apptentive.sdk.hostapptheme"
    const val USE_HOST_APP_THEME_KEY = "host_app_theme_key"

    const val THROTTLE_UTILS = "com.apptentive.sdk.throttle"

    const val CUSTOM_STORE_URL = "com.apptentive.sdk.customstoreurl"
    const val CUSTOM_STORE_URL_KEY = "custom_store_url_key"

    const val MESSAGE_CENTER_DRAFT = "com.apptentive.sdk.messagecenter.draft"
}
