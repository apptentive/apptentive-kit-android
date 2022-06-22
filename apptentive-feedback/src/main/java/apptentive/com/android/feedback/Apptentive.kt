package apptentive.com.android.feedback

import android.app.Application
import android.content.Context
import apptentive.com.android.concurrent.Executor
import apptentive.com.android.concurrent.ExecutorQueue
import apptentive.com.android.concurrent.Executors
import apptentive.com.android.core.AndroidApplicationInfo
import apptentive.com.android.core.AndroidExecutorFactoryProvider
import apptentive.com.android.core.AndroidLoggerProvider
import apptentive.com.android.core.ApplicationInfo
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.core.TimeInterval
import apptentive.com.android.core.format
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.interactions.InteractionId
import apptentive.com.android.feedback.message.DEFAULT_INTERNAL_EVENT_NAME
import apptentive.com.android.feedback.platform.AndroidFileSystemProvider
import apptentive.com.android.feedback.utils.SensitiveDataUtils
import apptentive.com.android.feedback.utils.ThrottleUtils
import apptentive.com.android.network.DefaultHttpClient
import apptentive.com.android.network.DefaultHttpNetwork
import apptentive.com.android.network.DefaultHttpRequestRetryPolicy
import apptentive.com.android.network.HttpClient
import apptentive.com.android.network.HttpLoggingInterceptor
import apptentive.com.android.network.HttpNetworkResponse
import apptentive.com.android.network.HttpRequest
import apptentive.com.android.network.asString
import apptentive.com.android.platform.SharedPrefConstants
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.FEEDBACK
import apptentive.com.android.util.LogTags.INTERACTIONS
import apptentive.com.android.util.LogTags.NETWORK
import apptentive.com.android.util.LogTags.PROFILE_DATA_UPDATE
import apptentive.com.android.util.LogTags.SYSTEM

sealed class EngagementResult {
    data class InteractionShown(val interactionId: InteractionId) : EngagementResult() {
        init { Log.d(INTERACTIONS, "Interaction Engaged => interactionID: $interactionId") }
    }
    data class InteractionNotShown(val description: String) : EngagementResult() {
        init { Log.d(INTERACTIONS, "Interaction NOT Engaged => $description") }
    }
    data class Error(val message: String) : EngagementResult() {
        init { Log.e(INTERACTIONS, "Interaction Engage Error => $message") }
    }
    data class Exception(val error: kotlin.Exception) : EngagementResult() {
        init { Log.e(INTERACTIONS, "Interaction Engage Exception => ${error.message}", error) }
    }
}

object Apptentive {
    private var client: ApptentiveClient = ApptentiveClient.NULL
    private var activityInfoCallback: ApptentiveActivityInfo? = null
    private lateinit var stateExecutor: Executor
    private lateinit var mainExecutor: Executor

    //region Initialization

    /**
     * collects the [ApptentiveActivityInfo] reference which can be used to retrieve current activity context.
     * The retrieved context is used in the apptentive interactions & it's UI elements.
     *
     * @param apptentiveActivityInfo
     */

    @JvmStatic
    fun registerApptentiveActivityInfoCallback(apptentiveActivityInfo: ApptentiveActivityInfo) {
        Log.d(FEEDBACK, "Activity info callback is registered")
        this.activityInfoCallback = apptentiveActivityInfo
    }

    /**
     * clears the [ApptentiveActivityInfo] reference
     */
    @JvmStatic
    fun unregisterApptentiveActivityInfoCallback() {
        Log.d(FEEDBACK, "Activity info callback is unregistered")
        this.activityInfoCallback = null
    }

    fun getApptentiveActivityCallback(): ApptentiveActivityInfo? = activityInfoCallback

    @Suppress("MemberVisibilityCanBePrivate")
    val registered
        @Synchronized get() = client != ApptentiveClient.NULL

    @JvmStatic
    @JvmName("register")
    @JvmOverloads
    @Synchronized
    fun _register(
        application: Application,
        configuration: ApptentiveConfiguration,
        callback: RegisterCallback? = null
    ) {
        // the above statement would not compile without force unwrapping on Kotlin 1.4.x
        @Suppress("UNNECESSARY_NOT_NULL_ASSERTION")
        val callbackFunc: ((RegisterResult) -> Unit)? =
            if (callback != null) callback!!::onComplete else null
        register(application, configuration, callbackFunc)
    }

    @Synchronized
    fun register(
        application: Application,
        configuration: ApptentiveConfiguration,
        callback: ((result: RegisterResult) -> Unit)? = null
    ) {
        if (registered) {
            Log.w(SYSTEM, "Apptentive SDK already registered")
            return
        }

        // register dependency providers
        DependencyProvider.register(AndroidLoggerProvider("Apptentive"))
        DependencyProvider.register<ApplicationInfo>(AndroidApplicationInfo(application.applicationContext))
        DependencyProvider.register(AndroidExecutorFactoryProvider())
        DependencyProvider.register(
            AndroidFileSystemProvider(
                application.applicationContext,
                "apptentive.com.android.feedback"
            )
        )

        // Save host app theme usage
        application.getSharedPreferences(SharedPrefConstants.USE_HOST_APP_THEME, Context.MODE_PRIVATE)
            .edit().putBoolean(SharedPrefConstants.USE_HOST_APP_THEME_KEY, configuration.shouldInheritAppTheme).apply()

        // Set log level
        Log.logLevel = configuration.logLevel

        // Set message redaction
        SensitiveDataUtils.shouldSanitizeLogMessages = configuration.shouldSanitizeLogMessages

        // Set rating throttle
        ThrottleUtils.ratingThrottleLength = configuration.ratingInteractionThrottleLength
        ThrottleUtils.throttleSharedPrefs =
            application.getSharedPreferences(SharedPrefConstants.THROTTLE_UTILS, Context.MODE_PRIVATE)

        // Save alternate app store URL to be set later
        application.getSharedPreferences(SharedPrefConstants.CUSTOM_STORE_URL, Context.MODE_PRIVATE)
            .edit().putString(SharedPrefConstants.CUSTOM_STORE_URL_KEY, configuration.customAppStoreURL).apply()

        Log.i(SYSTEM, "Registering Apptentive Android SDK ${Constants.SDK_VERSION}")
        Log.v(
            SYSTEM,
            "ApptentiveKey: ${SensitiveDataUtils.hideIfSanitized(configuration.apptentiveKey)} " +
                "ApptentiveSignature: ${SensitiveDataUtils.hideIfSanitized(configuration.apptentiveSignature)}"
        )

        stateExecutor = ExecutorQueue.createSerialQueue("SDK Queue")
        mainExecutor = ExecutorQueue.mainQueue

        // wrap the callback
        val callbackWrapper: ((RegisterResult) -> Unit)? = if (callback != null) {
            {
                mainExecutor.execute {
                    callback.invoke(it)
                }
            }
        } else null

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
                start(application.applicationContext, callbackWrapper)
            }
        }
    }

    private fun createHttpClient(context: Context): HttpClient {
        val loggingInterceptor = object : HttpLoggingInterceptor {
            override fun intercept(request: HttpRequest<*>) {
                Log.d(NETWORK, "--> ${request.method} ${request.url}")
                Log.v(NETWORK, "Headers:\n${SensitiveDataUtils.hideIfSanitized(request.headers)}")
                Log.v(NETWORK, "Request Body: ${SensitiveDataUtils.hideIfSanitized(request.requestBody?.asString())}")
            }

            override fun intercept(response: HttpNetworkResponse) {
                val statusCode = response.statusCode
                val statusMessage = response.statusMessage
                Log.d(NETWORK, "<-- $statusCode $statusMessage (${response.duration.format()} sec)")
                Log.v(NETWORK, "Response Body: ${SensitiveDataUtils.hideIfSanitized(response.asString())}")
            }

            override fun retry(request: HttpRequest<*>, delay: TimeInterval) {
                Log.d(
                    NETWORK,
                    "Retrying request ${request.method} ${request.url} in ${delay.format()} sec..."
                )
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

    //region Engagement

    @JvmStatic
    @JvmOverloads
    fun engage(eventName: String, callback: EngagementCallback? = null) {
        // the above statement would not compile without force unwrapping on Kotlin 1.4.x
        @Suppress("UNNECESSARY_NOT_NULL_ASSERTION")
        val callbackFunc: ((EngagementResult) -> Unit)? =
            if (callback != null) callback!!::onComplete else null
        engage(eventName, callbackFunc)
    }

    private fun engage(eventName: String, callback: ((EngagementResult) -> Unit)?) {
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
                val result = client.engage(event)
                callbackWrapper?.invoke(result)
            } catch (e: Exception) {
                callbackWrapper?.invoke(EngagementResult.Exception(error = e))
            }
        }
    }

    //endregion

    // region Person data updates

    /**
     * Sets the user's name. This name will be sent to the Apptentive server and displayed in conversations you have
     * with this person. This name will be the definitive username for this user, unless one is provided directly by the
     * user through an Apptentive UI. Calls to this method are idempotent. Calls to this method will overwrite any
     * previously entered person's name.
     *
     * @param name The user's name.
     */

    @JvmStatic
    fun setPersonName(name: String?) {
        stateExecutor.execute {
            name?.let { name ->
                client.updatePerson(name = name)
                if (name.isBlank()) {
                    Log.d(PROFILE_DATA_UPDATE, "Empty/Blank strings are not supported for name")
                }
            }
        }
    }

    /**
     * Sets the user's email address. This email address will be sent to the Apptentive server to allow out of app
     * communication, and to help provide more context about this user. This email will be the definitive email address
     * for this user, unless one is provided directly by the user through an Apptentive UI. Calls to this method are
     * idempotent. Calls to this method will overwrite any previously entered email, so if you don't want to overwrite
     * any previously entered email,
     *
     * @param email The user's email address.
     */

    @JvmStatic
    fun setPersonEmail(email: String?) {
        stateExecutor.execute {
            email?.let { email ->
                client.updatePerson(email = email)
                if (email.isBlank()) {
                    Log.d(PROFILE_DATA_UPDATE, "Empty/Blank strings are not supported for email")
                }
            }
        }
    }

    /**
     * Add a custom data String to the Person. Custom data will be sent to the server, is displayed
     * in the Conversation view, and can be used in Interaction targeting.  Calls to this method are
     * idempotent.
     *
     * @param key   The key to store the data under.
     * @param value A String value.
     */

    @JvmStatic
    fun addCustomPersonData(key: String?, value: String?) {
        stateExecutor.execute {
            if (key != null && value != null) {
                client.updatePerson(customData = Pair(key, value))
            }
        }
    }

    /**
     * Add a custom data Number to the Person. Custom data will be sent to the server, is displayed
     * in the Conversation view, and can be used in Interaction targeting.  Calls to this method are
     * idempotent.
     *
     * @param key   The key to store the data under.
     * @param value A Number value.
     */
    @JvmStatic
    fun addCustomPersonData(key: String?, value: Number?) {
        stateExecutor.execute {
            if (key != null && value != null) {
                client.updatePerson(customData = Pair(key, value.toString().toDouble()))
            }
        }
    }

    /**
     * Add a custom data Boolean to the Person. Custom data will be sent to the server, is displayed
     * in the Conversation view, and can be used in Interaction targeting.  Calls to this method are
     * idempotent.
     *
     * @param key   The key to store the data under.
     * @param value A Boolean value.
     */
    @JvmStatic
    fun addCustomPersonData(key: String?, value: Boolean?) {
        stateExecutor.execute {
            if (key != null && value != null) {
                client.updatePerson(customData = Pair(key, value))
            }
        }
    }

    /**
     * Remove a piece of custom data from the Person. Calls to this method are idempotent.
     *
     * @param key The key to remove.
     */
    @JvmStatic
    fun removeCustomPersonData(key: String?) {
        stateExecutor.execute {
            if (key != null) {
                client.updatePerson(deleteKey = key)
            }
        }
    }

    /**
     * Add a custom data String to the Device. Custom data will be sent to the server, is displayed
     * in the Conversation view, and can be used in Interaction targeting.  Calls to this method are
     * idempotent.
     *
     * @param key   The key to store the data under.
     * @param value A String value.
     */
    @JvmStatic
    fun addCustomDeviceData(key: String?, value: String?) {
        stateExecutor.execute {
            if (key != null && value != null) {
                client.updateDevice(customData = Pair(key, value))
            }
        }
    }

    /**
     * Add a custom data Number to the Device. Custom data will be sent to the server, is displayed
     * in the Conversation view, and can be used in Interaction targeting.  Calls to this method are
     * idempotent.
     *
     * @param key   The key to store the data under.
     * @param value A Number value.
     */
    @JvmStatic
    fun addCustomDeviceData(key: String?, value: Number?) {
        stateExecutor.execute {
            if (key != null && value != null) {
                client.updateDevice(customData = Pair(key, value.toString().toDouble()))
            }
        }
    }

    /**
     * Add a custom data Boolean to the Device. Custom data will be sent to the server, is displayed
     * in the Conversation view, and can be used in Interaction targeting.  Calls to this method are
     * idempotent.
     *
     * @param key   The key to store the data under.
     * @param value A Boolean value.
     */
    @JvmStatic
    fun addCustomDeviceData(key: String?, value: Boolean?) {
        stateExecutor.execute {
            if (key != null && value != null) {
                client.updateDevice(customData = Pair(key, value))
            }
        }
    }

    /**
     * Remove a piece of custom data from the device. Calls to this method are idempotent.
     *
     * @param key The key to remove.
     */
    @JvmStatic
    fun removeCustomDeviceData(key: String?) {
        stateExecutor.execute {
            if (key != null) {
                client.updateDevice(deleteKey = key)
            }
        }
    }
}
