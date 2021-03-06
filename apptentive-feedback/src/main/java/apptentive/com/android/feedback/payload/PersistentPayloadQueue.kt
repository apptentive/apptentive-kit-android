package apptentive.com.android.feedback.payload

import android.content.Context
import apptentive.com.android.feedback.utils.SensitiveDataUtils
import apptentive.com.android.feedback.utils.createStringTable
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogLevel
import apptentive.com.android.util.LogTags.PAYLOADS

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

    override fun deletePayload(payload: PayloadData) {
        dbHelper.deletePayload(payload.nonce)
        printPayloads("Delete payload")
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
                    "path",
                    "method",
                    "mediaType",
                    "data"
                )
            )
            val rows = payloads.map { payload ->
                arrayOf<Any?>(
                    payload.nonce,
                    payload.type,
                    payload.path,
                    payload.method,
                    payload.mediaType,
                    SensitiveDataUtils.hideIfSanitized(payload.data.toString(Charsets.UTF_8))
                )
            }
            Log.v(PAYLOADS, "$title (${payloads.size}):\n${createStringTable(header + rows)}")
        } catch (e: Exception) {
            Log.e(PAYLOADS, "Exception while printing payloads", e)
        }
    }

    companion object {
        fun create(context: Context) = PersistentPayloadQueue(
            dbHelper = PayloadSQLiteHelper(context)
        )
    }
}
