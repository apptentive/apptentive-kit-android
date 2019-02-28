package apptentive.com.android.feedback

import android.content.Context
import apptentive.com.android.concurrent.Executor

internal class ApptentiveDefaultFeedback(
    val apptentiveKey: String,
    val apptentiveSignature: String,
    private val executor: Executor
) : ApptentiveFeedback {
    internal fun start() {
    }

    override fun engage(context: Context, event: String) {
    }
}