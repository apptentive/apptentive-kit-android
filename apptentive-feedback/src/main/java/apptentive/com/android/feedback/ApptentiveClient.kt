package apptentive.com.android.feedback

import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.MESSAGE_CENTER
import apptentive.com.android.util.LogTags.MESSAGE_CENTER_HIDDEN
import apptentive.com.android.util.LogTags.PROFILE_DATA_UPDATE
import java.io.InputStream

internal interface ApptentiveClient {
    fun engage(event: Event, customData: Map<String, Any?>? = null): EngagementResult
    fun showMessageCenter(customData: Map<String, Any?>?): EngagementResult
    fun addUnreadMessagesListener(callback: UnreadMessageCallback)
    fun getUnreadMessageCount(): Int
    fun canShowMessageCenter(callback: (Boolean) -> Unit)
    fun sendHiddenTextMessage(message: String)
    fun sendHiddenAttachmentFileUri(uri: String)
    fun sendHiddenAttachmentFileBytes(bytes: ByteArray, mimeType: String)
    fun sendHiddenAttachmentFileStream(inputStream: InputStream, mimeType: String)
    fun updateDevice(customData: Pair<String, Any?>? = null, deleteKey: String? = null)
    fun updatePerson(name: String? = null, email: String? = null, customData: Pair<String, Any?>? = null, deleteKey: String? = null)

    companion object {
        val NULL: ApptentiveClient = ApptentiveNullClient()
    }
}

internal typealias UnreadMessageCallback = (Int) -> Unit

private class ApptentiveNullClient : ApptentiveClient {
    override fun engage(event: Event, customData: Map<String, Any?>?): EngagementResult {
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

    override fun showMessageCenter(customData: Map<String, Any?>?): EngagementResult {
        Log.d(MESSAGE_CENTER, "Apptentive SDK is not initialized; message center launch failed")
        return EngagementResult.Error("Apptentive SDK is not initialized")
    }

    override fun addUnreadMessagesListener(callback: UnreadMessageCallback) {
        Log.d(MESSAGE_CENTER, "Apptentive SDK is not initialized; add host unread message listener failed")
    }

    override fun getUnreadMessageCount(): Int {
        Log.d(MESSAGE_CENTER, "Apptentive SDK is not initialized; get unread message count failed")
        return 0
    }

    override fun canShowMessageCenter(callback: (Boolean) -> Unit) {
        Log.d(MESSAGE_CENTER, "Apptentive SDK is not initialized; can show message center check failed")
    }

    override fun sendHiddenAttachmentFileUri(uri: String) {
        Log.d(MESSAGE_CENTER_HIDDEN, "Apptentive SDK is not initialized; send attachment uri failed")
    }

    override fun sendHiddenAttachmentFileBytes(bytes: ByteArray, mimeType: String) {
        Log.d(MESSAGE_CENTER_HIDDEN, "Apptentive SDK is not initialized; send attachment bytes failed")
    }

    override fun sendHiddenAttachmentFileStream(inputStream: InputStream, mimeType: String) {
        Log.d(MESSAGE_CENTER_HIDDEN, "Apptentive SDK is not initialized; send attachment stream failed")
    }
}
