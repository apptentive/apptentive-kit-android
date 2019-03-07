package apptentive.com.android.feedback

import android.app.Application
import android.content.Context
import apptentive.com.android.concurrent.ExecutorQueue

object Apptentive {
    private var client: ApptentiveClient = ApptentiveNullClient
    private lateinit var stateQueue: ExecutorQueue

    fun register(application: Application, configuration: ApptentiveConfiguration) {
        // FIXME: do not allow multiple instances

        stateQueue = ExecutorQueue.createSerialQueue("apptentive")
        client = ApptentiveDefaultClient(
            configuration.apptentiveKey,
            configuration.apptentiveSignature
        ).apply {
            stateQueue.execute(::start)
        }
    }

    fun engage(context: Context, event: String) {
        client.engage(context, event)
    }
}