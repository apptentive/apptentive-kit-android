package apptentive.com.android.feedback.utils

enum class LogTag {
    NETWORK,
    APP_CONFIGURATION,
    CONVERSATION,
    INTERACTIONS,
    NOTIFICATIONS,
    MESSAGES,
    DATABASE,
    PAYLOADS,
    TESTER_COMMANDS,
    NOTIFICATION_INTERACTIONS,
    PUSH,
    UTIL,
    TROUBLESHOOT,
    ADVERTISER_ID,
    PARTNERS,
    SECURITY
}

fun logi(tag: LogTag, message: String) {
    println(message)
}