package apptentive.com.android.feedback.utils

import apptentive.com.android.core.platform.SharedPrefConstants
import apptentive.com.android.core.serialization.json.JsonConverter
import apptentive.com.android.core.util.Log
import apptentive.com.android.core.util.LogTags
import apptentive.com.android.feedback.platform.ApptentiveKitSDKState

/**
 * Some devices will restart the app while its in the background. The interaction's
 * model is the only dependency that cannot be recreated in that process. This backup
 * will attempt to retrieve the interaction from a sharedPreferences that is created
 * before the interaction launches.
 *
 * If it fails to create, don't throw an exception since this is an edge case fix.
 */

fun <T : Any> saveInteractionBackup(interactionModel: T) {
    Log.d(LogTags.INTERACTIONS, "Saving interaction model backup")

    try {
        val jsonModel = JsonConverter.toJson(interactionModel)

        ApptentiveKitSDKState.getSharedPrefDataStore().putString(
            SharedPrefConstants.APPTENTIVE,
            SharedPrefConstants.INTERACTION_BACKUP,
            jsonModel
        )
    } catch (exception: Exception) {
        Log.e(LogTags.INTERACTIONS, "Error converting interaction model for backup", exception)
    }
}

inline fun <reified T> getInteractionBackup(): T {
    Log.w(LogTags.INTERACTIONS, "Error creating ViewModel. Attempting backup.")

    try {
        val jsonInteraction = ApptentiveKitSDKState.getSharedPrefDataStore()
            .getString(SharedPrefConstants.APPTENTIVE, SharedPrefConstants.INTERACTION_BACKUP, "")
        return JsonConverter.fromJson(jsonInteraction)
    } catch (exception: Exception) {
        Log.e(LogTags.INTERACTIONS, "Error creating ViewModel. Backup failed.", exception)
        throw exception
    }
}

fun getWhereEventBackup(): String? {
    val whereEvent = ApptentiveKitSDKState.getSharedPrefDataStore().getNullableString(
        SharedPrefConstants.APPTENTIVE,
        SharedPrefConstants.WHERE_EVENT_BACKUP,
    )
    return whereEvent
}

fun saveWhereEventBackup(event: String) {
    ApptentiveKitSDKState.getSharedPrefDataStore().putString(
        SharedPrefConstants.APPTENTIVE,
        SharedPrefConstants.WHERE_EVENT_BACKUP,
        event
    )
}
