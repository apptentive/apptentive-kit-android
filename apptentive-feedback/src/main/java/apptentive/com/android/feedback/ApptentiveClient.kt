package apptentive.com.android.feedback

import android.content.Context
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.util.Log

internal interface ApptentiveClient {
    fun engage(context: Context, event: Event): EngagementResult
    fun updateDevice(customData: Pair<String, Any?>? = null, deleteKey: String? = null)
    fun updatePerson(name: String? = null, email: String? = null, customData: Pair<String, Any?>? = null, deleteKey: String? = null)

    companion object {
        val NULL: ApptentiveClient = ApptentiveNullClient()
    }
}

private class ApptentiveNullClient : ApptentiveClient {
    override fun engage(context: Context, event: Event): EngagementResult {
        return EngagementResult.Error("Apptentive SDK is not initialized") // TODO: better error message
    }

    override fun updatePerson(
        name: String?,
        email: String?,
        customData: Pair<String, Any?>?,
        deleteKey: String?
    ) {
        Log.d(PROFILE_DATA_UPDATE, "Apptentive SDK is not initialized; update person failed")
    }

    override fun updateDevice(customData: Pair<String, Any?>?, deleteKey: String?) {
        Log.d(PROFILE_DATA_UPDATE, "Apptentive SDK is not initialized; update device failed")
    }
}
