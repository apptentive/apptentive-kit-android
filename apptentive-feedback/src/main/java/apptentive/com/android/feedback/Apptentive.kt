package apptentive.com.android.feedback

import android.app.Application
import android.content.Context
import apptentive.com.android.concurrent.ExecutorQueue
import apptentive.com.android.core.DefaultExecutorQueueFactoryProvider
import apptentive.com.android.core.DefaultLoggerProvider
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.network.DefaultHttpClient
import apptentive.com.android.network.DefaultHttpNetwork
import apptentive.com.android.network.DefaultHttpRequestRetryPolicy
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
        DependencyProvider.register(DefaultLoggerProvider("Apptentive"))
        DependencyProvider.register(DefaultExecutorQueueFactoryProvider())

        stateQueue = ExecutorQueue.createSerialQueue("Apptentive")

        val httpClient = DefaultHttpClient(
            network = DefaultHttpNetwork(application.applicationContext),
            networkQueue = ExecutorQueue.createConcurrentQueue("Network"),
            callbackExecutor = stateQueue,
            retryPolicy = DefaultHttpRequestRetryPolicy()
        )

        // TODO: replace with a builder class and lift all the dependencies up
        client = ApptentiveDefaultClient(
            apptentiveKey = configuration.apptentiveKey,
            apptentiveSignature = configuration.apptentiveSignature,
            stateQueue = stateQueue,
            httpClient = httpClient
        ).apply {
            stateQueue.execute {
                start(application.applicationContext)
            }
        }
    }

    fun engage(context: Context, event: String) {
        stateQueue.execute {
            client.engage(context, event)
        }
    }
}