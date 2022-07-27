package apptentive.com.android.feedback

import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.model.CustomData
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.MESSAGE_CENTER
import apptentive.com.android.util.LogTags.PROFILE_DATA_UPDATE

internal interface ApptentiveClient {
    fun engage(event: Event): EngagementResult
    fun sendHiddenTextMessage(message: String)
    fun showMessageCenter(callback: EngagementCallback? = null, customData: CustomData?)
    fun updateDevice(customData: Pair<String, Any?>? = null, deleteKey: String? = null)
    fun updatePerson(name: String? = null, email: String? = null, customData: Pair<String, Any?>? = null, deleteKey: String? = null)

    companion object {
        val NULL: ApptentiveClient = ApptentiveNullClient()
    }
}

private class ApptentiveNullClient : ApptentiveClient {
    override fun engage(event: Event): EngagementResult {
        return EngagementResult.Error("Apptentive SDK is not initialized")
    }

    override fun updatePerson(
        name: String?,
        email: String?,
        customData: Pair<String, Any?>?,
        deleteKey: String?
    ) {
        Log.d(PROFILE_DATA_UPDATE, "Apptentive SDK is not initialized; update person failed")
    }

    override fun sendHiddenTextMessage(message: String) {
        Log.d(MESSAGE_CENTER, "Apptentive SDK is not initialized; send attachment text failed")
    }

    override fun updateDevice(customData: Pair<String, Any?>?, deleteKey: String?) {
        Log.d(PROFILE_DATA_UPDATE, "Apptentive SDK is not initialized; update device failed")
    }

    override fun showMessageCenter(callback: EngagementCallback?, customData: CustomData?) {
        Log.d(MESSAGE_CENTER, "Apptentive SDK is not initialized; message center launch failed")
    }
}
