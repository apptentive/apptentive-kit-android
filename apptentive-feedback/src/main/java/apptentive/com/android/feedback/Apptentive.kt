package apptentive.com.android.feedback

import android.app.Application
import android.content.Context
import apptentive.com.android.concurrent.ExecutorQueue
import apptentive.com.android.core.DependencyProvider

object Apptentive {
    private var client: ApptentiveClient = ApptentiveClient.NULL
    private lateinit var stateQueue: ExecutorQueue

    val registered get() = client != ApptentiveClient.NULL

    fun register(application: Application, configuration: ApptentiveConfiguration) {
        // FIXME: do not allow multiple instances
        DependencyProvider.register(application)

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