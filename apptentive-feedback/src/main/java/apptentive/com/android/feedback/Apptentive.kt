package apptentive.com.android.feedback

import android.app.Application
import android.content.Context
import apptentive.com.android.concurrent.ExecutorQueue
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.network.DefaultHttpNetwork

object Apptentive {
    private var client: ApptentiveClient = ApptentiveClient.NULL
    private lateinit var stateQueue: ExecutorQueue

    val registered get() = client != ApptentiveClient.NULL

    fun register(application: Application, configuration: ApptentiveConfiguration) {
        // FIXME: do not allow multiple instances
        DependencyProvider.register(application)

        stateQueue = ExecutorQueue.createSerialQueue("Apptentive")
        client = ApptentiveDefaultClient(
            apptentiveKey = configuration.apptentiveKey,
            apptentiveSignature = configuration.apptentiveSignature,
            stateQueue = stateQueue,
            network = DefaultHttpNetwork(application.applicationContext)
        ).apply {
            stateQueue.execute {
                start(application.applicationContext)
            }
        }
    }

    fun engage(context: Context, event: String) {
        client.engage(context, event)
    }
}