package apptentive.com.android.feedback

import android.app.Application
import android.content.Context
import apptentive.com.android.concurrent.Executor
import apptentive.com.android.concurrent.ExecutorQueue

object Apptentive {
    private var feedback: ApptentiveFeedback = ApptentiveNullFeedback
    private lateinit var executor: Executor

    fun register(application: Application, configuration: ApptentiveConfiguration) {
        // TODO: do not allow multiple instances

        executor = ExecutorQueue.createSerialQueue("feedback")
        feedback = ApptentiveDefaultFeedback(
            configuration.apptentiveKey,
            configuration.apptentiveSignature,
            executor
        ).apply {
            executor.execute(::start)
        }
    }

    fun engage(context: Context, event: String) {
        feedback.engage(context, event)
    }
}