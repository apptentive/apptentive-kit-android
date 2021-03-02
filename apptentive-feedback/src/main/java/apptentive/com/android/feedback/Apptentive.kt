package apptentive.com.android.feedback

import android.app.Application
import android.content.Context
import apptentive.com.android.concurrent.Executor
import apptentive.com.android.concurrent.ExecutorQueue
import apptentive.com.android.concurrent.Executors
import apptentive.com.android.core.*
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.interactions.InteractionId
import apptentive.com.android.network.*
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags
import apptentive.com.android.util.LogTags.network

// TODO: better names for specific cases
sealed class EngagementResult {
    data class Success(val interactionId: InteractionId) : EngagementResult()
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
    @JvmStatic
    fun register(application: Application, configuration: ApptentiveConfiguration) {
        if (registered) {
            Log.w(SYSTEM, "Apptentive SDK already registered")
            return
        }

        // register dependency providers
        DependencyProvider.register(AndroidLoggerProvider("Apptentive"))
        DependencyProvider.register(AndroidExecutorFactoryProvider())
        DependencyProvider.register(AndroidFileSystemProvider(application.applicationContext, "apptentive.com.android.feedback"))

        // set log level
        Log.logLevel = configuration.logLevel

        stateExecutor = ExecutorQueue.createSerialQueue("SDK Queue")
        mainExecutor = ExecutorQueue.mainQueue

        // TODO: build a better dependency injection solution and lift all the dependencies up
        client = ApptentiveDefaultClient(
            apptentiveKey = configuration.apptentiveKey,
            apptentiveSignature = configuration.apptentiveSignature,
            httpClient = createHttpClient(application.applicationContext),
            executors = Executors(
                state = stateExecutor,
                main = mainExecutor
            )
        ).apply {
            stateExecutor.execute {
                start(application.applicationContext)
            }
        }
    }

    private fun createHttpClient(context: Context): HttpClient {
        val loggingInterceptor = object: HttpLoggingInterceptor {
            override fun intercept(request: HttpRequest<*>) {
                Log.d(network, "--> ${request.method} ${request.url}")
                Log.v(network, "Headers:\n${request.headers}")
                Log.v(network, "Request Body: ${request.requestBody?.asString()}")
            }

            override fun intercept(response: HttpNetworkResponse) {
                val statusCode = response.statusCode
                val statusMessage = response.statusMessage
                Log.d(network, "<-- $statusCode $statusMessage (${response.duration.format()} sec)")
                Log.v(network, "Response Body: ${response.asString()}")
            }

            override fun retry(request: HttpRequest<*>, delay: TimeInterval) {
                Log.d(network, "Retrying request ${request.method} ${request.url} in ${delay.format()} sec...")
            }
        }
        return DefaultHttpClient(
            network = DefaultHttpNetwork(context),
            networkQueue = ExecutorQueue.createConcurrentQueue("Network"),
            callbackExecutor = stateExecutor,
            retryPolicy = DefaultHttpRequestRetryPolicy(),
            loggingInterceptor = loggingInterceptor
        )
    }

    //endregion
    @JvmStatic
    @JvmOverloads
    fun engage(context: Context, eventName: String, callback: EngagementCallback? = null) {
        // the above statement would not compile without force unwrapping on Kotlin 1.4.x
        @Suppress("UNNECESSARY_NOT_NULL_ASSERTION")
        val callbackFunc: ((EngagementResult) -> Unit)? = if (callback != null) callback!!::onComplete else null
        engage(context, eventName, callbackFunc)
    }

    fun engage(context: Context, eventName: String, callback: ((EngagementResult) -> Unit)?) {
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

    //region Debug

    // FIXME: extract to 'debug' module
    fun reset() {
        stateExecutor.execute {
            val client = client as? ApptentiveDefaultClient
            if (client != null) {
                client.reset()
            } else {
                Log.e(LogTags.core, "Unable to clear event: sdk is not initialized")
            }
        }
    }

    //endregion
}