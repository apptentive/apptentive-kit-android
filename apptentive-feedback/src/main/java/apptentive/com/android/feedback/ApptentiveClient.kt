package apptentive.com.android.feedback

import android.content.Context
import apptentive.com.android.feedback.engagement.Event

internal interface ApptentiveClient {
    fun engage(context: Context, event: Event): EngagementResult

    companion object {
        val NULL: ApptentiveClient = ApptentiveNullClient()
    }
}

private class ApptentiveNullClient : ApptentiveClient {
    override fun engage(context: Context, event: Event): EngagementResult {
        return EngagementResult.Error("Apptentive SDK is not initialized") // TODO: better error message
    }
}