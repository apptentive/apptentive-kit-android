package apptentive.com.android.feedback

import android.content.Context
import apptentive.com.android.util.Log

internal interface ApptentiveFeedback {
    fun engage(context: Context, event: String)
}

internal object ApptentiveNullFeedback : ApptentiveFeedback {
    override fun engage(context: Context, event: String) {
        printError("engage '$event' event")
    }

    private fun printError(action: String) {
        Log.e(feedback, "Unable to $action: Apptentive SDK is not properly initialized.")
    }
}