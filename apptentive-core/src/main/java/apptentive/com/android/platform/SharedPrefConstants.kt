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
    const val CONVERSATION_RESET_THROTTLE = "conversation_reset_throttle"

    const val CUSTOM_STORE_URL = "com.apptentive.sdk.customstoreurl"
    const val CUSTOM_STORE_URL_KEY = "custom_store_url_key"

    const val MESSAGE_CENTER_DRAFT = "com.apptentive.sdk.messagecenter.draft"
    const val MESSAGE_CENTER_DRAFT_TEXT = "message.text"
    const val MESSAGE_CENTER_DRAFT_ATTACHMENTS = "message.attachments"
    const val MESSAGE_CENTER_PROFILE_NAME = "profile.name"
    const val MESSAGE_CENTER_PROFILE_EMAIL = "profile.email"

    const val SDK_CORE_INFO = "com.apptentive.sdk.coreinfo"
    const val SDK_VERSION = "sdk_version"
    const val CRYPTO_ENABLED = "should_encrypt"
    const val CRYPTO_KEY_ALIAS = "crypto.key.alias"
}
