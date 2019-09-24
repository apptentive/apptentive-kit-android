package apptentive.com.android.feedback

import android.app.Application
import android.content.Context
import apptentive.com.android.concurrent.ExecutorQueue
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.core.ExecutorQueueFactoryProvider
import apptentive.com.android.core.PlatformLoggerProvider
import apptentive.com.android.network.DefaultHttpNetwork
import apptentive.com.android.util.Log

object Apptentive {
    private var client: ApptentiveClient = ApptentiveClient.NULL
    private lateinit var stateQueue: ExecutorQueue

    val registered @Synchronized get() = client != ApptentiveClient.NULL

    @Synchronized
    fun register(application: Application, configuration: ApptentiveConfiguration) {
        if (registered) {
            Log.w(SYSTEM, "Apptentive SDK already registered")
            return
        }

        // register dependency providers
        DependencyProvider.register(PlatformLoggerProvider("Apptentive"))
        DependencyProvider.register(ExecutorQueueFactoryProvider())

        stateQueue = ExecutorQueue.createSerialQueue("Apptentive")

        // TODO: replace with a builder class and lift all the dependencies up
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