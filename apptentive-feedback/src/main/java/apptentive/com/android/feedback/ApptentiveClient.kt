package apptentive.com.android.feedback

import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.EVENT
import apptentive.com.android.util.LogTags.FEEDBACK
import apptentive.com.android.util.LogTags.MESSAGE_CENTER
import apptentive.com.android.util.LogTags.MESSAGE_CENTER_HIDDEN
import apptentive.com.android.util.LogTags.PROFILE_DATA_GET
import apptentive.com.android.util.LogTags.PROFILE_DATA_UPDATE
import java.io.InputStream

internal interface ApptentiveClient {
    fun engage(event: Event, customData: Map<String, Any?>? = null): EngagementResult
    fun canShowInteraction(event: Event): Boolean
    fun showMessageCenter(customData: Map<String, Any?>?): EngagementResult
    fun getUnreadMessageCount(): Int
    fun canShowMessageCenter(): Boolean
    fun sendHiddenTextMessage(message: String)
    fun sendHiddenAttachmentFileUri(uri: String)
    fun sendHiddenAttachmentFileBytes(bytes: ByteArray, mimeType: String)
    fun sendHiddenAttachmentFileStream(inputStream: InputStream, mimeType: String)
    fun updateDevice(customData: Pair<String, Any?>? = null, deleteKey: String? = null)
    fun updatePerson(name: String? = null, email: String? = null, customData: Pair<String, Any?>? = null, deleteKey: String? = null)
    fun updateMParticleID(id: String)
    fun getPersonName(): String?
    fun getPersonEmail(): String?
    fun setPushIntegration(pushProvider: Int, token: String)
    fun setLocalManifest(json: String)

    companion object {
        val NULL: ApptentiveClient = ApptentiveNullClient()
    }
}

internal typealias UnreadMessageCallback = (Int) -> Unit

private class ApptentiveNullClient : ApptentiveClient {
    override fun engage(event: Event, customData: Map<String, Any?>?): EngagementResult {
        return EngagementResult.Error("Apptentive SDK is not initialized. Cannot engage event: ${event.name}")
    }

    override fun canShowInteraction(event: Event): Boolean {
        Log.d(EVENT, "Apptentive SDK is not initialized. Cannot show interaction.")
        return false
    }

    override fun updatePerson(
        name: String?,
        email: String?,
        customData: Pair<String, Any?>?,
        deleteKey: String?
    ) {
        Log.d(PROFILE_DATA_UPDATE, "Apptentive SDK is not initialized; update person failed")
    }

    override fun updateMParticleID(id: String) {
        Log.d(PROFILE_DATA_UPDATE, "Apptentive SDK is not initialized; set mParticle id failed")
    }

    override fun sendHiddenTextMessage(message: String) {
        Log.d(MESSAGE_CENTER, "Apptentive SDK is not initialized; send attachment text failed")
    }

    override fun setPushIntegration(pushProvider: Int, token: String) {
        Log.d(PROFILE_DATA_UPDATE, "Apptentive SDK is not initialized; set push integration")
    }

    override fun updateDevice(customData: Pair<String, Any?>?, deleteKey: String?) {
        Log.d(PROFILE_DATA_UPDATE, "Apptentive SDK is not initialized; update device failed")
    }

    override fun showMessageCenter(customData: Map<String, Any?>?): EngagementResult {
        Log.d(MESSAGE_CENTER, "Apptentive SDK is not initialized; message center launch failed")
        return EngagementResult.Error("Apptentive SDK is not initialized")
    }

    override fun getUnreadMessageCount(): Int {
        Log.d(MESSAGE_CENTER, "Apptentive SDK is not initialized; get unread message count failed")
        return 0
    }

    override fun canShowMessageCenter(): Boolean {
        Log.d(MESSAGE_CENTER, "Apptentive SDK is not initialized; can show message center check failed")
        return false
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

    override fun getPersonName(): String? {
        Log.d(PROFILE_DATA_GET, "Apptentive SDK is not initialized; get person name failed")
        return null
    }

    override fun getPersonEmail(): String? {
        Log.d(PROFILE_DATA_GET, "Apptentive SDK is not initialized; get person email failed")
        return null
    }

    override fun setLocalManifest(uri: String) {
        Log.d(FEEDBACK, "Apptentive SDK is not initialized; set local manifest failed ")
    }
}
