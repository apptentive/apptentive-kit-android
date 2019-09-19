package apptentive.com.android.feedback

import android.content.Context
import apptentive.com.android.util.Log

internal interface ApptentiveClient {
    fun engage(context: Context, event: String)

    companion object {
        val NULL: ApptentiveClient = ApptentiveNullClient()
    }
}

private class ApptentiveNullClient : ApptentiveClient {
    override fun engage(context: Context, event: String) {
        printError("engage '$event' event")
    }

    private fun printError(action: String) {
        Log.e(FEEDBACK, "Unable to $action: Apptentive SDK is not properly initialized.")
    }
}