package apptentive.com.android.feedback

import android.app.Application
import android.content.Context
import apptentive.com.android.concurrent.Executor
import apptentive.com.android.concurrent.ExecutorQueue
import apptentive.com.android.core.DefaultExecutorQueueFactoryProvider
import apptentive.com.android.core.DefaultLoggerProvider
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.network.DefaultHttpClient
import apptentive.com.android.network.DefaultHttpNetwork
import apptentive.com.android.network.DefaultHttpRequestRetryPolicy
import apptentive.com.android.util.Log

sealed class EngagementResult {
    object Success : EngagementResult()
    data class Failure(val reason: String) : EngagementResult()
}

object Apptentive {
    private var client: ApptentiveClient = ApptentiveClient.NULL
    private lateinit var stateExecutor: Executor
    private lateinit var mainExecutor: Executor

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

        stateExecutor = ExecutorQueue.createSerialQueue("Apptentive")
        mainExecutor = ExecutorQueue.mainQueue

        val httpClient = DefaultHttpClient(
            network = DefaultHttpNetwork(application.applicationContext),
            networkQueue = ExecutorQueue.createConcurrentQueue("Network"),
            callbackExecutor = stateExecutor,
            retryPolicy = DefaultHttpRequestRetryPolicy()
        )

        // TODO: replace with a builder class and lift all the dependencies up
        client = ApptentiveDefaultClient(
            apptentiveKey = configuration.apptentiveKey,
            apptentiveSignature = configuration.apptentiveSignature,
            httpClient = httpClient
        ).apply {
            stateExecutor.execute {
                start(application.applicationContext)
            }
        }
    }

    fun engage(context: Context, eventName: String, callback: ((EngagementResult) -> Unit)? = null) {
        // user callback should be executed on the main thread
        val callbackWrapper: ((EngagementResult) -> Unit)? = if (callback != null) {
            {
                mainExecutor.execute {
                    callback.invoke(it)
                }
            }
        } else null

        // all the SDK related operations should be executed on the state executor
        stateExecutor.execute {
            try {
                val event = Event.local(eventName)
                val result = client.engage(context, event)
                callbackWrapper?.invoke(result)
            } catch (e: Exception) {
                callbackWrapper?.invoke(EngagementResult.Failure("Exception while engaging '$eventName' event: ${e.message}"))
            }
        }
    }
}