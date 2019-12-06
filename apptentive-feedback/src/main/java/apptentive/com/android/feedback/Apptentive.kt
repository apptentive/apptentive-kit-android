package apptentive.com.android.feedback

import android.app.Application
import android.content.Context
import apptentive.com.android.concurrent.Executor
import apptentive.com.android.concurrent.ExecutorQueue
import apptentive.com.android.core.AndroidFileSystemProvider
import apptentive.com.android.core.DefaultExecutorQueueFactoryProvider
import apptentive.com.android.core.AndroidLoggerProvider
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.network.DefaultHttpClient
import apptentive.com.android.network.DefaultHttpNetwork
import apptentive.com.android.network.DefaultHttpRequestRetryPolicy
import apptentive.com.android.network.HttpClient
import apptentive.com.android.util.Log

// TODO: better names for specific cases
sealed class EngagementResult {
    object Success : EngagementResult()
    data class Failure(val description: String) : EngagementResult()
    data class Error(val message: String) : EngagementResult()
    data class Exception(val error: kotlin.Exception) : EngagementResult()
}

object Apptentive {
    private var client: ApptentiveClient = ApptentiveClient.NULL
    private lateinit var stateExecutor: Executor
    private lateinit var mainExecutor: Executor

    //region Initialization

    @Suppress("MemberVisibilityCanBePrivate")
    val registered @Synchronized get() = client != ApptentiveClient.NULL

    @Synchronized
    fun register(application: Application, configuration: ApptentiveConfiguration) {
        if (registered) {
            Log.w(SYSTEM, "Apptentive SDK already registered")
            return
        }

        // register dependency providers
        DependencyProvider.register(AndroidLoggerProvider("Apptentive"))
        DependencyProvider.register(DefaultExecutorQueueFactoryProvider())
        DependencyProvider.register(AndroidFileSystemProvider(application.applicationContext, "apptentive.com.android.feedback"))

        stateExecutor = ExecutorQueue.createSerialQueue("Apptentive")
        mainExecutor = ExecutorQueue.mainQueue

        // TODO: build a better dependency injection solution and lift all the dependencies up
        client = ApptentiveDefaultClient(
            apptentiveKey = configuration.apptentiveKey,
            apptentiveSignature = configuration.apptentiveSignature,
            httpClient = createHttpClient(application.applicationContext)
        ).apply {
            stateExecutor.execute {
                start(application.applicationContext)
            }
        }
    }

    private fun createHttpClient(context: Context): HttpClient {
        return DefaultHttpClient(
            network = DefaultHttpNetwork(context),
            networkQueue = ExecutorQueue.createConcurrentQueue("Network"),
            callbackExecutor = stateExecutor,
            retryPolicy = DefaultHttpRequestRetryPolicy()
        )
    }

    //endregion

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
                callbackWrapper?.invoke(EngagementResult.Exception(error = e))
            }
        }
    }
}