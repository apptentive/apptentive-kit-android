package apptentive.com.android.feedback.payload

import android.content.Context
import android.os.Build
import apptentive.com.android.encryption.Encryption
import apptentive.com.android.feedback.conversation.ConversationCredentialProvider
import apptentive.com.android.feedback.utils.FileUtil
import apptentive.com.android.feedback.utils.createStringTable
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogLevel
import apptentive.com.android.util.LogTags.PAYLOADS
import java.util.Base64

internal class PersistentPayloadQueue(
    private val dbHelper: PayloadSQLiteHelper
) : PayloadQueue {
    override fun enqueuePayload(payload: PayloadData) {
        dbHelper.addPayload(payload)
        printPayloads("Enqueue payload")
    }

    override fun nextUnsentPayload(): PayloadData? {
        return dbHelper.nextUnsentPayload()
    }

    override fun deletePayloadAndAssociatedFiles(payload: PayloadData) {
        FileUtil.deleteFile(payload.sidecarData.dataFilePath)
        dbHelper.deletePayload(payload.nonce)
        printPayloads("Delete payload and associated files")
    }

    override fun updateCredential(credentialProvider: ConversationCredentialProvider) {
        dbHelper.updateCredential(credentialProvider)
    }

    override fun invalidateCredential(tag: String) {
        dbHelper.invalidateCredential(tag)
    }

    private fun printPayloads(title: String) {
        if (!Log.canLog(LogLevel.Verbose)) {
            // avoid unnecessary computations
            return
        }

        try {
            val payloads = dbHelper.readPayloads()
            if (payloads.isEmpty()) {
                Log.v(PAYLOADS, "$title (0)")
                return
            }

            val header = listOf(
                arrayOf<Any?>(
                    "nonce",
                    "type",
                    "tag",
                    "token",
                    "data"
                )
            )
            val rows = payloads.map { payload ->
                val formattedData = when {
                    payload.data.size > 5000 -> "Request body too large to print."
                    payload.mediaType == MediaType.applicationJson ||
                        Build.VERSION.SDK_INT < Build.VERSION_CODES.O -> String(
                        payload.data,
                        Charsets.UTF_8
                    )

                    else -> "Binary data: ${Base64.getEncoder().encodeToString(payload.data)}"
                }

                val formattedToken = when (payload.token) {
                    null -> "null"
                    "embedded" -> "embedded"
                    else -> "JWT"
                }

                arrayOf<Any?>(
                    payload.nonce,
                    payload.type,
                    payload.tag,
                    formattedToken,
                    formattedData
                )
            }
            Log.v(PAYLOADS, "$title (${payloads.size}):\n${createStringTable(header + rows)}")
        } catch (e: Exception) {
            Log.e(PAYLOADS, "Exception while printing payloads", e)
        }
    }

    companion object {
        fun create(context: Context, encryption: Encryption, clearCache: Boolean) = PersistentPayloadQueue(
            dbHelper = PayloadSQLiteHelper(context, encryption).apply {
                if (clearCache) deleteAllCachedPayloads()
            }
        )
    }
}
