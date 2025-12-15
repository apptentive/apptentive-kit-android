package apptentive.com.android.feedback

import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import apptentive.com.android.concurrent.Executor
import apptentive.com.android.concurrent.ExecutorQueue
import apptentive.com.android.concurrent.Executors
import apptentive.com.android.core.AndroidApplicationInfo
import apptentive.com.android.core.AndroidExecutorFactoryProvider
import apptentive.com.android.core.AndroidLoggerProvider
import apptentive.com.android.core.ApplicationInfo
import apptentive.com.android.core.ApptentiveException
import apptentive.com.android.core.BehaviorSubject
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.core.Observable
import apptentive.com.android.core.TimeInterval
import apptentive.com.android.core.format
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.interactions.InteractionId
import apptentive.com.android.feedback.model.EventNotification
import apptentive.com.android.feedback.model.MessageCenterNotification
import apptentive.com.android.feedback.notifications.NotificationUtils
import apptentive.com.android.feedback.platform.AndroidFileSystemProvider
import apptentive.com.android.feedback.platform.DefaultStateMachine
import apptentive.com.android.feedback.platform.SDKEvent
import apptentive.com.android.feedback.platform.SDKState
import apptentive.com.android.feedback.platform.isSDKLoading
import apptentive.com.android.feedback.utils.SensitiveDataUtils
import apptentive.com.android.feedback.utils.ThrottleUtils
import apptentive.com.android.feedback.utils.isMarshmallowOrGreater
import apptentive.com.android.feedback.utils.sha256
import apptentive.com.android.network.DefaultHttpClient
import apptentive.com.android.network.DefaultHttpNetwork
import apptentive.com.android.network.DefaultHttpRequestRetryPolicy
import apptentive.com.android.network.HttpClient
import apptentive.com.android.network.HttpLoggingInterceptor
import apptentive.com.android.network.HttpNetworkResponse
import apptentive.com.android.network.HttpRequest
import apptentive.com.android.platform.AndroidSharedPrefDataStore
import apptentive.com.android.platform.DefaultAndroidSharedPrefDataStore
import apptentive.com.android.platform.SharedPrefConstants
import apptentive.com.android.util.InternalUseOnly
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags
import apptentive.com.android.util.LogTags.CONVERSATION
import apptentive.com.android.util.LogTags.DEVICE
import apptentive.com.android.util.LogTags.FEEDBACK
import apptentive.com.android.util.LogTags.INTERACTIONS
import apptentive.com.android.util.LogTags.MESSAGE_CENTER
import apptentive.com.android.util.LogTags.NETWORK
import apptentive.com.android.util.LogTags.PERSON
import apptentive.com.android.util.LogTags.PROFILE_DATA_UPDATE
import apptentive.com.android.util.LogTags.PUSH_NOTIFICATION
import apptentive.com.android.util.LogTags.SYSTEM
import org.json.JSONException
import org.json.JSONObject
import java.io.InputStream
import java.lang.ref.WeakReference

/**
 * Result class used in a callback for the `engage` function to help understand what happens.
 *
 * [InteractionShown]    Event was evaluated through criteria and an Apptentive Interaction
 *                       was shown as a result. Returns with ID of interaction shown.
 *
 * [InteractionNotShown] Event was evaluated through criteria and an Apptentive Interaction
 *                       was NOT shown as a result. Returns with [String] reasoning
 *
 * [Error]               Event was evaluated through criteria and an interaction was supposed to show,
 *                       but an error occurred in that process. Can also show if the SDK fails to
 *                       initialize. Returns with [String] reasoning.
 *
 * [Exception]           At some point in the evaluation or interaction showing process a [Throwable]
 *                       was thrown. Returns with [String] error message and prints error stacktrace.
 */
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
    private var activityInfoCallback: WeakReference<ApptentiveActivityInfo>? = null
    private lateinit var stateExecutor: Executor
    private lateinit var mainExecutor: Executor

    //region Initialization

    /**
     * Collects the [ApptentiveActivityInfo] reference which can be used to retrieve the
     * current Activity's [Context].
     * The retrieved context is used in the Apptentive interactions & its UI elements.
     *
     * @param apptentiveActivityInfo reference to the app's current Activity
     */

    @JvmStatic
    fun registerApptentiveActivityInfoCallback(apptentiveActivityInfo: ApptentiveActivityInfo) {
        if (registered) {
            try {
                activityInfoCallback = WeakReference(apptentiveActivityInfo)

                Log.d(
                    FEEDBACK,
                    "Activity info callback for ${
                    activityInfoCallback?.get()?.getApptentiveActivityInfo()?.localClassName
                    } registered"
                )
            } catch (exception: Exception) {
                Log.e(FEEDBACK, "Exception thrown while registered activity info callback", exception)
            }
        }
    }

    /**
     * Util method to check if the [ApptentiveActivityInfo] is registered
     */
    fun isApptentiveActivityInfoCallbackRegistered() = activityInfoCallback != null

    /**
     * Optional method:
     * Clears the [ApptentiveActivityInfo] [WeakReference]
     */
    @JvmStatic
    fun unregisterApptentiveActivityInfoCallback() {
        if (registered) {
            try {
                Log.d(
                    FEEDBACK,
                    "Activity info callback for ${
                    activityInfoCallback?.get()?.getApptentiveActivityInfo()?.localClassName
                    } unregistered"
                )
                activityInfoCallback = null
            } catch (exception: Exception) {
                Log.e(FEEDBACK, "Exception thrown while unregistering activity info callback")
            }
        }
    }

    @InternalUseOnly
    fun getApptentiveActivityCallback(): ApptentiveActivityInfo? = activityInfoCallback?.get()

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

    /**
     * This method registers the Apptentive SDK using the given SDK credentials in the [ApptentiveConfiguration]
     * It must be called from the  Application#onCreate() method in the Application object defined in your app's manifest.
     *
     * @param application Application object.
     * @param configuration [ApptentiveConfiguration] containing SDK initialization data.
     * @param callback Returns a callback of an [RegisterResult].
     */
    @Synchronized
    fun register(
        application: Application,
        configuration: ApptentiveConfiguration,
        callback: ((result: RegisterResult) -> Unit)? = null
    ) {
        if (registered) {
            Log.w(SYSTEM, "Apptentive SDK already registered")
            callback?.invoke(RegisterResult.Failure("Apptentive SDK already registered", -1))
        } else {
            DefaultStateMachine.onEvent(SDKEvent.RegisterSDK)

            try {

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
                DependencyProvider.register<AndroidSharedPrefDataStore>(
                    DefaultAndroidSharedPrefDataStore(application.applicationContext)
                )

                checkSavedKeyAndSignature(configuration)

                // Save host app theme usage

                DependencyProvider.of<AndroidSharedPrefDataStore>().putBoolean(
                    SharedPrefConstants.USE_HOST_APP_THEME,
                    SharedPrefConstants.USE_HOST_APP_THEME_KEY,
                    configuration.shouldInheritAppTheme
                )

                // Set log level
                Log.logLevel = configuration.logLevel

                // Set message redaction
                SensitiveDataUtils.shouldSanitizeLogMessages =
                    configuration.shouldSanitizeLogMessages

                // Set rating throttle
                ThrottleUtils.ratingThrottleLength = configuration.ratingInteractionThrottleLength
                ThrottleUtils.interactionCountLimit = configuration.perSessionInteractionLimit

                // Save alternate app store URL to be set later

                DependencyProvider.of<AndroidSharedPrefDataStore>().putString(
                    SharedPrefConstants.CUSTOM_STORE_URL,
                    SharedPrefConstants.CUSTOM_STORE_URL_KEY,
                    configuration.customAppStoreURL
                )

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
                    configuration = configuration,
                    httpClient = createHttpClient(application.applicationContext),
                    executors = Executors(
                        state = stateExecutor,
                        main = mainExecutor
                    ),
                ).apply {
                    stateExecutor.execute {
                        initialize(application.applicationContext)
                        start(application.applicationContext, callbackWrapper)
                    }
                }
            } catch (exception: Exception) {
                Log.e(FEEDBACK, "Exception thrown in the SDK registration", exception)
                DefaultStateMachine.onEvent(SDKEvent.Error)
            }
        }
    }

    /**
     * Starts login process asynchronously. This call returns immediately. Using this method requires
     * you to implement JWT generation on your server. Please read about it in Apptentive's Android
     * Integration Reference Guide.
     *
     * @param jwtToken A JWT signed by your server using the secret from your app's Apptentive settings.
     * @param callback A LoginCallback, which will be called asynchronously when the login succeeds
     *                 or fails.
     */
    @JvmStatic
    @Synchronized
    fun login(jwtToken: String, callback: ((result: LoginResult) -> Unit)? = null) {
        when {
            Build.VERSION_CODES.M > Build.VERSION.SDK_INT -> {
                Log.w(FEEDBACK, "Login is only supported on Android M and above")
                executeCallbackInMainExecutor(callback, LoginResult.Error("Login is only supported on Android M and above"))
            }
            DefaultStateMachine.state == SDKState.LOGGED_IN -> {
                Log.w(FEEDBACK, "The SDK is already logged in. Logout first to login again")
                executeCallbackInMainExecutor(callback, LoginResult.Error("The SDK is already logged in. Logout first to login again"))
            }
            DefaultStateMachine.state == SDKState.UNINITIALIZED -> {
                Log.w(FEEDBACK, "The SDK is not initialized yet. Please register the SDK first")
                executeCallbackInMainExecutor(callback, LoginResult.Error("The SDK is not initialized yet. Please register the SDK first"))
            }
            else -> {
                try {
                    stateExecutor.execute {
                        client.login(jwtToken, callback)
                    }
                } catch (e: java.lang.Exception) {
                    Log.e(FEEDBACK, "Exception thrown in the SDK login", e)
                    executeCallbackInMainExecutor(callback, LoginResult.Exception(e))
                }
            }
        }
    }

    /**
     * Returns whether or not the register() method is already called.
     *
     * @return true if the SDK's register method is already called, false otherwise.
     */
    fun isRegistered(): Boolean = registered

    internal fun executeCallbackInMainExecutor(callback: ((result: LoginResult) -> Unit)? = null, result: LoginResult) {
        mainExecutor.execute {
            callback?.invoke(result)
        }
    }

    /*
     * Starts logout process asynchronously.
     * The current logged in user will be logged out.
     * The SDK will stop sending events & showing interactions until a new user is logged in.
     */
    @JvmStatic
    @Synchronized
    fun logout() {
        try {
            if (DefaultStateMachine.state != SDKState.LOGGED_IN) {
                Log.e(FEEDBACK, "The SDK is not logged in. Nothing to logout", ApptentiveException("The SDK is not logged in. Nothing to logout"))
                return
            }
            stateExecutor.execute {
                client.logout()
            }
        } catch (e: java.lang.Exception) {
            Log.e(FEEDBACK, "Exception thrown in the SDK logout", e)
        }
    }

    /**
     * Registers your listener with Apptentive. This listener is stored with a WeakReference, which
     * means that you must store a static reference to the listener as long as you want it to live.
     * One possible way to do this is to implement this listener with your Application class, or store
     * one on your Application.
     *
     *
     * This listener will alert you to authentication failures, so that you can either recover from
     * expired or revoked JWTs, or fix your authentication implementation.
     *
     * @param listener A listener that will be called when there is an authentication failure other
     * for the current logged in conversation. If the failure is for another
     * conversation, or there is no active conversation, the listener is not called.
     */
    fun setAuthenticationFailedListener(listener: AuthenticationFailedListener) {
        try {
            client.setAuthenticationFailedListener(listener)
        } catch (e: java.lang.Exception) {
            Log.e(CONVERSATION, e, "Error in Apptentive.setUnreadMessagesListener()")
        }
    }

    /*
     * Refreshes the auth token for the logged in user
     *
     * @param jwtToken A JWT signed by your server using the secret from your app's Apptentive settings.
     * @param callback A LoginCallback, which will be called asynchronously when the login succeeds
     */
    @JvmStatic
    @Synchronized
    fun updateToken(jwtToken: String, callback: ((result: LoginResult) -> Unit)? = null) {
        when {
            !isMarshmallowOrGreater() -> {
                Log.w(FEEDBACK, "Login is only supported on Android M and above")
                callback?.invoke(LoginResult.Error("Login is only supported on Android M and above"))
            }
            DefaultStateMachine.state != SDKState.LOGGED_IN -> {
                Log.w(FEEDBACK, "Need to login first to update token")
                callback?.invoke(LoginResult.Error("Need to login first to update token"))
            }
            else -> {
                try {
                    stateExecutor.execute {
                        client.updateToken(jwtToken, callback)
                    }
                } catch (e: java.lang.Exception) {
                    Log.e(FEEDBACK, "Exception thrown in the SDK udpate token", e)
                    callback?.invoke(LoginResult.Exception(e))
                }
            }
        }
    }

    // Not a public API, internal use only
    @InternalUseOnly
    fun getCurrentState(): SDKState {
        return DefaultStateMachine.state
    }

    private fun checkSavedKeyAndSignature(
        configuration: ApptentiveConfiguration
    ) {
        val registrationSharedPrefs = DependencyProvider.of<AndroidSharedPrefDataStore>()

        val savedKeyHash =
            registrationSharedPrefs.getNullableString(SharedPrefConstants.REGISTRATION_INFO, SharedPrefConstants.APPTENTIVE_KEY_HASH, null)
        val savedSignatureHash =
            registrationSharedPrefs.getNullableString(SharedPrefConstants.REGISTRATION_INFO, SharedPrefConstants.APPTENTIVE_SIGNATURE_HASH, null)

        if (savedKeyHash.isNullOrEmpty() && savedSignatureHash.isNullOrEmpty()) {
            registrationSharedPrefs
                .putString(
                    SharedPrefConstants.REGISTRATION_INFO,
                    SharedPrefConstants.APPTENTIVE_KEY_HASH,
                    configuration.apptentiveKey.sha256()
                )
            registrationSharedPrefs
                .putString(
                    SharedPrefConstants.REGISTRATION_INFO,
                    SharedPrefConstants.APPTENTIVE_SIGNATURE_HASH,
                    configuration.apptentiveSignature.sha256()
                )
            Log.d(LogTags.CONFIGURATION, "Saving current ApptentiveKey and ApptentiveSignature hash")
        } else {
            val newKeyHash = configuration.apptentiveKey.sha256()
            val newSignatureHash = configuration.apptentiveSignature.sha256()
            val errorMessage = when {
                newKeyHash != savedKeyHash && newSignatureHash != savedSignatureHash -> {
                    "ApptentiveKey and ApptentiveSignature do not match saved ApptentiveKey and ApptentiveSignature"
                }
                newKeyHash != savedKeyHash -> {
                    "ApptentiveKey does not match saved ApptentiveKey"
                }
                newSignatureHash != savedSignatureHash -> {
                    "ApptentiveSignature does not match saved ApptentiveSignature"
                }
                else -> {
                    /* Key & Signature match. Do nothing */
                    null
                }
            }
            errorMessage?.let {
                Log.w(LogTags.CONFIGURATION, errorMessage)
            }
        }
    }

    private fun createHttpClient(context: Context): HttpClient {
        val loggingInterceptor = object : HttpLoggingInterceptor {
            override fun intercept(request: HttpRequest<*>) {
                Log.d(NETWORK, "--> ${request.method} ${request.url}")
                Log.v(NETWORK, "Headers:\n${SensitiveDataUtils.hideIfSanitized(request.headers)}")
                Log.v(NETWORK, "Content-Type: ${request.requestBody?.contentType}")
                Log.v(
                    NETWORK,
                    "Request Body: ${SensitiveDataUtils.hideIfSanitized(request.requestBody?.asString())}"
                )
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

    /**
     * This method takes a unique event of type [String], stores a record of that event having been
     * visited, determines if there is an interaction that is able to run for this event, and then
     * runs it. Only one interaction at most will run per invocation of this method. This task is
     * performed asynchronously.
     *
     * @param eventName  A unique [String] representing the line this method is called on. For instance,
     *                   you may want to have the ability to target interactions to run after the user
     *                   uploads a file in your app. You may then call `engage("finished_upload")`.
     * @param customData Extra data sent with the engaged event. A Map of [String] keys to values.
     *                   Values may be of type [String], [Number], or [Boolean].
     * @param callback   Returns [EngagementCallback] of an [EngagementResult].
     */
    @JvmStatic
    @JvmOverloads
    fun engage(eventName: String, customData: Map<String, Any?>? = null, callback: EngagementCallback? = null) {
        // the above statement would not compile without force unwrapping on Kotlin 1.4.x
        try {
            val callbackFunc: ((EngagementResult) -> Unit)? =
                if (callback != null) callback::onComplete else null
            engage(eventName, customData, callbackFunc)
        } catch (exception: Exception) {
            Log.e(FEEDBACK, "Exception when engage the event $eventName", exception)
            callback?.onComplete(EngagementResult.Error("There was an exception when engaging the event $eventName"))
        }
    }

    private fun engage(eventName: String, customData: Map<String, Any?>?, callback: ((EngagementResult) -> Unit)?) {
        // user callback should be executed on the main thread
        val callbackWrapper: ((EngagementResult) -> Unit)? = if (callback != null) {
            {
                mainExecutor.execute {
                    callback.invoke(it)
                }
            }
        } else null
        if (DefaultStateMachine.state == SDKState.LOGGED_OUT) {
            Log.w(FEEDBACK, "SDK is in logged out state. Please login to engage an event")
            callbackWrapper?.invoke(EngagementResult.Error("SDK is in logged out state. Please login to engage an event"))
        } else {
            // all the SDK related operations should be executed on the state executor
            stateExecutor.execute {
                try {
                    val event = Event.local(eventName)
                    val result = client.engage(event, customData)
                    callbackWrapper?.invoke(result)
                } catch (exception: Exception) {
                    callbackWrapper?.invoke(EngagementResult.Exception(error = exception))
                }
            }
        }
    }

    /**
     * Returns whether or not an engage event will display an Interaction.
     *
     * See the [Can Show Interaction](https://learn.apptentive.com/knowledge-base/android-integration-guide/#can-show-interaction)
     * section of the Apptentive Learn documentation for more info.
     */
    @JvmStatic
    fun canShowInteraction(eventName: String): Boolean {
        val event = Event.local(eventName)
        return client.canShowInteraction(event)
    }

    /**
     * Exempt event(s) from rate limit and won't throttle
     */
    @JvmStatic
    internal fun excludeEventsFromThrottling(events: List<String>) {
        if (isRegistered()) {
            try {
                stateExecutor.execute {
                    client.excludeEventsFromThrottling(events)
                }
            } catch (e: Exception) {
                Log.e(FEEDBACK, "Exception while excluding events from throttling", e)
            }
        } else {
            Log.w(FEEDBACK, "SDK is not registered")
        }
    }

    //endregion

    //region Event Notifications

    /**
     * Allows the event stream of the Apptentive SDK to be observed.
     *
     * See the [Event Monitoring](https://learn.apptentive.com/knowledge-base/android-integration-guide/#event-monitoring)
     * section of the Apptentive Learn documentation for more info.
     */
    internal val eventNotificationSubject: BehaviorSubject<EventNotification?> = BehaviorSubject(null)
    @JvmStatic
    val eventNotificationObservable: Observable<EventNotification?> get() = eventNotificationSubject

    /**
     * Allows a stream of Message Center data to be observed.
     *
     * See the [Message Center Data Monitoring](https://learn.apptentive.com/knowledge-base/android-integration-guide/#message-center-data-monitoring)
     * section of the Apptentive Learn documentation for more info.
     */
    internal val messageCenterNotificationSubject = BehaviorSubject(
        if (registered) MessageCenterNotification() else null
    )
    @JvmStatic
    val messageCenterNotificationObservable: Observable<MessageCenterNotification?> get() = messageCenterNotificationSubject

    //endregion

    //region Message Center

    /**
     * Opens the Apptentive Message Center.
     * This operation is performed asynchronously.
     *
     * @param customData Extra data sent with messages in Message Center. A Map of [String] keys
     *                   to values. Values may be of type [String], [Number], or [Boolean].
     * @param callback   Returns [EngagementCallback] of an [EngagementResult]
     */
    @JvmStatic
    @JvmOverloads
    fun showMessageCenter(customData: Map<String, Any?>? = null, callback: EngagementCallback? = null) {
        val callbackWrapper: ((EngagementResult) -> Unit)? = if (callback != null) {
            {
                mainExecutor.execute {
                    callback.onComplete(it)
                }
            }
        } else null

        when {
            DefaultStateMachine.state == SDKState.LOGGED_OUT -> {
                Log.w(FEEDBACK, "SDK is in logged out state. Please login to show message center")
                callbackWrapper?.invoke(EngagementResult.Error("SDK is in logged out state. Please login to show message center"))
            }
            !registered || DefaultStateMachine.state == SDKState.ERROR -> {
                Log.w(FEEDBACK, "SDK is not registered or have reached an error state")
                callbackWrapper?.invoke(EngagementResult.Error("SDK is not registered or have reached an error state"))
            }
            else -> {
                stateExecutor.execute {
                    try {
                        val result = client.showMessageCenter(customData)
                        callbackWrapper?.invoke(result)
                    } catch (exception: Exception) {
                        callbackWrapper?.invoke(EngagementResult.Exception(error = exception))
                        Log.e(MESSAGE_CENTER, "Exception showing the message center", exception)
                    }
                }
            }
        }
    }

    /**
     * Our SDK must connect to our server at least once to download initial configuration for Message
     * Center. Call this method to see whether or not Message Center can be displayed. This task is
     * performed asynchronously.
     *
     * @param callback Called after we check to see if Message Center can be displayed, but before it
     * is displayed. Called with true Message Center will be displayed, else false.
     */
    @JvmStatic
    fun canShowMessageCenter(callback: BooleanCallback) {
        try {
            callback.onFinish(canShowMessageCenter())
        } catch (exception: Exception) {
            Log.e(MESSAGE_CENTER, "Exception while checking canShowMessageCenter", exception)
        }
    }

    @JvmStatic
    fun canShowMessageCenter(): Boolean {
        return client.canShowMessageCenter()
    }

    /**
     * Returns the number of unread messages in the Message Center.
     *
     * This should be called after Apptentive is started. If this is called before Apptentive
     * finishes initializing **for the first time** then it may not work.
     *
     * @return The number of unread messages.
     */
    @JvmStatic
    fun getUnreadMessageCount(): Int {
        return try {
            if (DefaultStateMachine.state == SDKState.LOGGED_OUT) {
                Log.w(FEEDBACK, "SDK is in logged out state. Please login to get unread message count")
            } else {
                client.getUnreadMessageCount()
            }
            client.getUnreadMessageCount()
        } catch (exception: Exception) {
            Log.w(MESSAGE_CENTER, "Exception while getting unread message count", exception)
            0
        }
    }

    /**
     * Sends a text message to the server. This message will be visible in the conversation view
     * on the server, but will not be shown in the client's Message Center.
     *
     * @param text The message you wish to send.
     */
    @JvmStatic
    fun sendAttachmentText(text: String?) {
        try {
            stateExecutor.execute {
                if (DefaultStateMachine.state == SDKState.LOGGED_OUT) {
                    Log.w(FEEDBACK, "SDK is in logged out state. Please login to get unread message count")
                } else {
                    text?.let {
                        client.sendHiddenTextMessage(text)
                    } ?: run {
                        Log.d(MESSAGE_CENTER, "Attachment text was null")
                    }
                }
            }
        } catch (exception: Exception) {
            Log.e(MESSAGE_CENTER, "Exception sending text to server", exception)
        }
    }

    /**
     * Sends a file to the server. This file will be visible in the conversation view on the server,
     * but will not be shown in the client's Message Center. A local copy of this file will be made
     * until the message is transmitted, at which point the temporary file will be deleted.
     *
     * NOTICE: FILE SIZE LIMIT IS 15MB
     *
     * @param uri The URI path of the local resource file.
     */
    @JvmStatic
    fun sendAttachmentFile(uri: String?) {
        try {
            if (!uri.isNullOrBlank()) {
                stateExecutor.execute {
                    if (DefaultStateMachine.state == SDKState.LOGGED_OUT) {
                        Log.w(FEEDBACK, "SDK is in logged out state. Please login to send attachment file")
                    } else {
                        client.sendHiddenAttachmentFileUri(uri)
                    }
                }
            } else Log.d(MESSAGE_CENTER, "URI String was null or blank. URI: $uri")
        } catch (exception: Exception) {
            Log.e(MESSAGE_CENTER, "Exception while trying to send attachment file to server", exception)
        }
    }

    /**
     * Sends a file to the server. This file will be visible in the conversation view on the server,
     * but will not be shown in the client's Message Center. A local copy of this file will be made
     * until the message is transmitted, at which point the temporary file will be deleted.
     *
     * NOTICE: FILE SIZE LIMIT IS 15MB
     *
     * @param content  A byte array of the file contents.
     * @param mimeType The mime type of the file.
     */
    @JvmStatic
    fun sendAttachmentFile(content: ByteArray?, mimeType: String?) {
        try {
            if (content != null && mimeType != null) {
                stateExecutor.execute {
                    if (DefaultStateMachine.state == SDKState.LOGGED_OUT) {
                        Log.w(FEEDBACK, "SDK is in logged out state. Please login to send attachment file")
                    } else {
                        client.sendHiddenAttachmentFileBytes(content, mimeType)
                    }
                }
            } else Log.d(
                MESSAGE_CENTER,
                "Content and Mime Type cannot be null. Content: $content, mimeType: $mimeType"
            )
        } catch (exception: Exception) {
            Log.e(MESSAGE_CENTER, "Exception while trying to send attachment file", exception)
        }
    }

    /**
     * Sends a file to the server. This file will be visible in the conversation view on the server,
     * but will not be shown in the client's Message Center. A local copy of this file will be made
     * until the message is transmitted, at which point the temporary file will be deleted.
     *
     * NOTICE: FILE SIZE LIMIT IS 15MB
     *
     * @param inputStream An InputStream from the desired file.
     * @param mimeType    The mime type of the file.
     */
    @JvmStatic
    fun sendAttachmentFile(inputStream: InputStream?, mimeType: String?) {
        try {
            if (inputStream != null && mimeType != null) {
                stateExecutor.execute {
                    if (DefaultStateMachine.state == SDKState.LOGGED_OUT) {
                        Log.w(FEEDBACK, "SDK is in logged out state. Please login to send attachment file")
                    } else {
                        client.sendHiddenAttachmentFileStream(inputStream, mimeType)
                    }
                }
            } else Log.d(
                MESSAGE_CENTER,
                "InputStream and Mime Type cannot be null\ninputStream: $inputStream, mimeType: $mimeType"
            )
        } catch (exception: Exception) {
            Log.e(MESSAGE_CENTER, "Exception while trying to send attachment file", exception)
        }
    }

    //endregion

    //region Person data updates

    /**
     * Sets the user's name. This name will be sent to the Apptentive server and displayed in
     * conversations you have with this person. This name will be the definitive username for this
     * user, unless one is provided directly by the user through an Apptentive UI. Calls to this
     * method are idempotent. Calls to this method will overwrite any previously entered person's name.
     * You can check the value with [getPersonName].
     *
     * `null` or blank strings will fail to be set on the server so we don't attempt.
     *
     * @param name The user's name.
     */

    @JvmStatic
    fun setPersonName(name: String?) {
        try {
            stateExecutor.execute {
                when {
                    DefaultStateMachine.state == SDKState.LOGGED_OUT -> {
                        Log.w(FEEDBACK, "SDK is in logged out state. Please login to set person name")
                    }
                    !name.isNullOrBlank() -> client.updatePerson(name = name)
                    else -> Log.d(
                        PROFILE_DATA_UPDATE,
                        "Null or Empty/Blank strings are not supported for name"
                    )
                }
            }
        } catch (exception: Exception) {
            Log.e(PERSON, "Exception setting Person's name", exception)
        }
    }

    /**
     * Retrieves the user's name. This name may be set via [setPersonName],
     * or by the user through Message Center.
     *
     * @return The person's name if set, else `null`.
     */
    @JvmStatic
    fun getPersonName(): String? {
        return try {
            when {
                DefaultStateMachine.state == SDKState.LOGGED_OUT -> {
                    Log.w(FEEDBACK, "SDK is in logged out state. Please login to get person name")
                    null
                }
                registered -> client.getPersonName()
                else -> {
                    Log.w(
                        LogTags.PROFILE_DATA_GET,
                        "Apptentive not registered. Cannot get Person name."
                    )
                    null
                }
            }
        } catch (exception: Exception) {
            Log.e(PERSON, "Exception while getting Person's name", exception)
            null
        }
    }

    /**
     * Sets the user's email address. This email address will be sent to the Apptentive server to
     * allow out of app communication, and to help provide more context about this user. This email
     * will be the definitive email address for this user, unless one is provided directly by the
     * user through an Apptentive UI. Calls to this method are idempotent. Calls to this method will
     * overwrite any previously entered email, so if you don't want to overwrite any previously
     * entered email. You can check the value with [getPersonEmail].
     *
     * `null` or blank strings will fail to be set on the server so we don't attempt.
     *
     * @param email The user's email address.
     */

    @JvmStatic
    fun setPersonEmail(email: String?) {
        try {
            stateExecutor.execute {
                if (!email.isNullOrBlank()) client.updatePerson(email = email)
                else Log.d(
                    PROFILE_DATA_UPDATE,
                    "Null or Empty/Blank strings are not supported for email"
                )
            }
        } catch (exception: Exception) {
            Log.e(PERSON, "Exception while setting Person's email", exception)
        }
    }

    /**
     * Retrieves the user's email address. This address may be set via [setPersonEmail],
     * or by the user through Message Center.
     *
     * @return The person's email if set, else `null`.
     */
    @JvmStatic
    fun getPersonEmail(): String? {
        return try {
            if (registered) client.getPersonEmail()
            else {
                Log.w(
                    LogTags.PROFILE_DATA_GET,
                    "Apptentive not registered. Cannot get Person email."
                )
                null
            }
        } catch (exception: Exception) {
            Log.e(PERSON, "Exception while getting Person's email", exception)
            null
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
        try {
            stateExecutor.execute {
                if (key != null && value != null) {
                    client.updatePerson(customData = Pair(key, value))
                }
            }
        } catch (exception: Exception) {
            Log.e(PERSON, "Exception while adding a String custom person data with a key $key", exception)
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
        try {
            stateExecutor.execute {
                if (key != null && value != null) {
                    client.updatePerson(customData = Pair(key, value.toString().toDouble()))
                }
            }
        } catch (exception: Exception) {
            Log.e(PERSON, "Exception while adding Number custom person data with the key $key", exception)
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
        try {
            stateExecutor.execute {
                if (key != null && value != null) {
                    client.updatePerson(customData = Pair(key, value))
                }
            }
        } catch (exception: Exception) {
            Log.e(PERSON, "Exception while adding Boolean custom person data with the key $key", exception)
        }
    }

    /**
     * Remove a piece of custom data from the Person. Calls to this method are idempotent.
     *
     * @param key The key to remove.
     */
    @JvmStatic
    fun removeCustomPersonData(key: String?) {
        try {
            stateExecutor.execute {
                if (key != null) {
                    client.updatePerson(deleteKey = key)
                }
            }
        } catch (exception: Exception) {
            Log.e(PERSON, "Exception when removing a custom person data with the key $key", exception)
        }
    }

    /**
     * Sets the mParticleId of the user.
     * It is used to associate a MParticle user with a conversation in Apptentive dashboard
     *
     * @param id The mParticleId of the person.
     */
    @JvmStatic
    fun setMParticleId(id: String) {
        try {
            stateExecutor.execute {
                client.updateMParticleID(id)
            }
        } catch (exception: Exception) {
            Log.e(PERSON, "Exception setting MParticle ID", exception)
        }
    }

    //endregion

    //region Device data updates

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
        try {
            stateExecutor.execute {
                if (key != null && value != null) {
                    client.updateDevice(customData = Pair(key, value))
                }
            }
        } catch (exception: Exception) {
            Log.e(DEVICE, "Exception when adding String Device data with the key $key", exception)
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
        try {
            stateExecutor.execute {
                if (key != null && value != null) {
                    client.updateDevice(customData = Pair(key, value.toString().toDouble()))
                }
            }
        } catch (exception: Exception) {
            Log.e(DEVICE, "Exception when adding Number Device data with the key $key", exception)
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
        try {
            stateExecutor.execute {
                if (key != null && value != null) {
                    client.updateDevice(customData = Pair(key, value))
                }
            }
        } catch (exception: Exception) {
            Log.e(DEVICE, "Exception when adding Boolean Device data with the key $key", exception)
        }
    }

    /**
     * Remove a piece of custom data from the device. Calls to this method are idempotent.
     *
     * @param key The key to remove.
     */
    @JvmStatic
    fun removeCustomDeviceData(key: String?) {
        try {
            stateExecutor.execute {
                if (key != null) {
                    client.updateDevice(deleteKey = key)
                }
            }
        } catch (exception: Exception) {
            Log.e(DEVICE, "Exception when removing Device data with the key $key", exception)
        }
    }

    //endregion

    //region Push Notifications
    /**
     * Use this ID when you call `notify` for Apptentive Push Notifications if you want
     * notifications to be cleared when when Message Center is shown.
     */
    const val APPTENTIVE_NOTIFICATION_ID = 1056 // Sum of "Apptentive" to ASCII values

    /**
     * Call [setPushNotificationIntegration] with one of the below values to allow Apptentive to
     * send push notifications to this device.
     *
     * * [PUSH_PROVIDER_APPTENTIVE]     - Requires a valid Firebase Cloud Messaging (FCM) configuration.
     * * [PUSH_PROVIDER_PARSE]          - Requires a valid Parse integration.
     * * [PUSH_PROVIDER_URBAN_AIRSHIP]  - Requires a valid Urban Airship Push integration.
     * * [PUSH_PROVIDER_AMAZON_AWS_SNS] - Requires a valid Amazon Web Services (AWS) Simple Notification Service (SNS) integration.
     */
    const val PUSH_PROVIDER_APPTENTIVE = 0
    const val PUSH_PROVIDER_PARSE = 1
    const val PUSH_PROVIDER_URBAN_AIRSHIP = 2
    const val PUSH_PROVIDER_AMAZON_AWS_SNS = 3

    /**
     * Sends push provider information to our server to allow us to send pushes to this device when
     * you reply to your customers. Only one push provider is allowed to be active at a time, so you
     * should only call this method once. Please see our
     * [integration guide](http://www.apptentive.com/docs/android/integration-guide/#push-notifications)
     * for instructions.
     *
     * @param pushProvider One of the following:
     *  * [PUSH_PROVIDER_APPTENTIVE]
     *  * [PUSH_PROVIDER_PARSE]
     *  * [PUSH_PROVIDER_URBAN_AIRSHIP]
     *  * [PUSH_PROVIDER_AMAZON_AWS_SNS]
     *
     * @param token The push provider token you receive from your push provider. The format is push provider specific.
     * * Apptentive - Pass in the FCM Registration ID, which you can
     * [access like this](https://firebase.google.com/docs/cloud-messaging/android/client#monitor-token-generation).
     * * Urban Airship - The Urban Airship Channel ID, which you can
     * [access like this](https://github.com/urbanairship/android-samples/blob/4569d317a48efb2f23541fd8f8f87f03b3474c72/app/src/main/java/com/urbanairship/sample/SampleAirshipReceiver.java#L49).
     * * Parse - The Parse [deviceToken](https://docs.parseplatform.org/android/guide/#push-notifications)
     * * Amazon AWS SNS - The FCM Registration ID, which you can [access like this](https://firebase.google.com/docs/cloud-messaging/android/client#monitor-token-generation).
     */
    @JvmStatic
    fun setPushNotificationIntegration(context: Context, pushProvider: Int, token: String) {
        if (DefaultStateMachine.isSDKLoading()) {
            Log.w(
                PUSH_NOTIFICATION,
                "Apptentive is not initialized. Cannot set push notification integration."
            )
            return
        }
        try {
            stateExecutor.execute {
                context
                    .getSharedPreferences(SharedPrefConstants.APPTENTIVE, Context.MODE_PRIVATE)
                    .edit()
                    .putInt(SharedPrefConstants.PREF_KEY_PUSH_PROVIDER, pushProvider)
                    .putString(SharedPrefConstants.PREF_KEY_PUSH_TOKEN, token)
                    .apply()

                client.setPushIntegration(pushProvider, token)
            }
        } catch (exception: Exception) {
            Log.e(PUSH_NOTIFICATION, " Exception while setting push notification integration", exception)
        }
    }

    /**
     * Determines whether this Intent is a push notification sent from Apptentive.
     *
     * @param intent The received [Intent] you received in your BroadcastReceiver.
     * @return `true` if the [Intent] came from, and should be handled by Apptentive.
     */
    @JvmStatic
    fun isApptentivePushNotification(intent: Intent?): Boolean {
        try {
            return registered && NotificationUtils.getApptentivePushNotificationData(intent) != null
        } catch (exception: Exception) {
            Log.e(
                PUSH_NOTIFICATION,
                "Exception while checking for Apptentive push notification intent",
                exception
            )
        }
        return false
    }

    /**
     * Determines whether push payload data came from an Apptentive Push Notification.
     * This method is also used for Urban Airship.
     *
     * @param data The push payload data obtained through FCM `onMessageReceived()`, when using FCM
     * @return `true` if the push came from, and should be handled by Apptentive.
     */
    @JvmStatic
    fun isApptentivePushNotification(data: Map<String, String>?): Boolean {
        try {
            return registered && NotificationUtils.getApptentivePushNotificationData(data) != null
        } catch (exception: Exception) {
            Log.e(
                PUSH_NOTIFICATION,
                "Exception while checking for Apptentive push notification data",
                exception
            )
        }
        return false
    }

    /**
     *
     * Use this method in your push receiver to build a [PendingIntent] when an Apptentive push
     * notification is received. Pass the generated [PendingIntent] to
     * [androidx.core.app.NotificationCompat.Builder.setContentIntent] to allow Apptentive
     * to display Interactions such as Message Center. Calling this method for a push [Intent] that
     * did not come from Apptentive will return a null object. If you receive a `null` object, your
     * app will need to handle this notification itself.
     *
     * This task is performed asynchronously.
     *
     * This is the method you will likely need if you integrated using Urban Airship, AWS/SNS, or Parse
     *
     * @param callback Called after we check to see Apptentive can launch an Interaction from this
     * push. Called with a [PendingIntent] to launch an Apptentive Interaction
     * if the push data came from Apptentive, and an Interaction can be shown, or
     * `null`.
     * @param intent An [Intent] containing the Apptentive Push data. Pass in what you receive
     * in the Service or BroadcastReceiver that is used by your chosen push provider.
     */
    @JvmStatic
    fun buildPendingIntentFromPushNotification(context: Context, callback: PendingIntentCallback, intent: Intent) {
        try {
            if (registered) {
                stateExecutor.execute {
                    val apptentivePushData =
                        NotificationUtils.getApptentivePushNotificationData(intent)
                    val pendingIntent =
                        NotificationUtils.generatePendingIntentFromApptentivePushData(
                            context,
                            client as ApptentiveDefaultClient,
                            apptentivePushData
                        )
                    mainExecutor.execute {
                        callback.onPendingIntent(pendingIntent)
                    }
                }
            } else {
                Log.w(PUSH_NOTIFICATION, "Apptentive is not registered. Cannot build Push Intent.")
            }
        } catch (exception: Exception) {
            Log.e(PUSH_NOTIFICATION, "Exception while building pending Intent from push notification", exception)
        }
    }

    /**
     *
     * Use this method in your push receiver to build a pending Intent when an Apptentive push
     * notification is received. Pass the generated PendingIntent to
     * [androidx.core.app.NotificationCompat.Builder.setContentIntent] to allow Apptentive
     * to display Interactions such as Message Center. Calling this method for a push [Bundle] that
     * did not come from Apptentive will return a null object. If you receive a null object, your app
     * will need to handle this notification itself.
     *
     * This task is performed asynchronously.
     *
     * This is the method you will likely need if you integrated using Firebase Cloud Messaging
     * or Urban Airship
     *
     * @param callback Called after we check to see Apptentive can launch an Interaction from this
     * push. Called with a [PendingIntent] to launch an Apptentive Interaction if the push data
     * came from Apptentive, and an Interaction can be shown, or `null`.
     * @param data A [Map]<[String], [String]>; containing the Apptentive
     * Push data. Pass in what you receive in the the Service or BroadcastReceiver
     * that is used by your chosen push provider.
     */
    @JvmStatic
    fun buildPendingIntentFromPushNotification(context: Context, callback: PendingIntentCallback, data: Map<String, String>) {
        try {
            if (registered) {
                stateExecutor.execute {
                    val apptentivePushData =
                        NotificationUtils.getApptentivePushNotificationData(data)
                    val intent = NotificationUtils.generatePendingIntentFromApptentivePushData(
                        context,
                        client as ApptentiveDefaultClient,
                        apptentivePushData
                    )
                    mainExecutor.execute {
                        callback.onPendingIntent(intent)
                    }
                }
            } else {
                Log.w(PUSH_NOTIFICATION, "Apptentive is not registered. Cannot build Push Intent.")
            }
        } catch (exception: Exception) {
            Log.e(PUSH_NOTIFICATION, "Exception while building pending intent from push notification", exception)
        }
    }

    /**
     * Use this method in your push receiver to get the notification title you can use to construct
     * an [android.app.Notification] object.
     *
     * @param intent An [Intent] containing the Apptentive Push data. Pass in what you receive
     * in the Service or BroadcastReceiver that is used by your chosen push provider.
     * @return a [String] value, or `null`.
     */
    @JvmStatic
    fun getTitleFromApptentivePush(intent: Intent?): String? {
        return try {
            if (registered && intent != null) {
                getTitleFromApptentivePush(intent.extras)
            } else null
        } catch (exception: Exception) {
            Log.e(PUSH_NOTIFICATION, "Exception while getting title from Apptentive push", exception)
            null
        }
    }

    /**
     * Use this method in your push receiver to get the notification body text you can use to
     * construct an [android.app.Notification] object.
     *
     * @param intent An [Intent] containing the Apptentive Push data. Pass in what you receive
     * in the Service or BroadcastReceiver that is used by your chosen push provider.
     * @return a [String] value, or `null`.
     */
    @JvmStatic
    fun getBodyFromApptentivePush(intent: Intent?): String? {
        return try {
            if (registered && intent != null) {
                getBodyFromApptentivePush(intent.extras)
            } else null
        } catch (exception: Exception) {
            Log.e(PUSH_NOTIFICATION, "Exception while getting body from Apptentive push", exception)
            null
        }
    }

    /**
     * Use this method in your push receiver to get the notification title you can use to construct a
     * [android.app.Notification] object.
     *
     * @param bundle A [Bundle] containing the Apptentive Push data. Pass in what you receive in
     * the the Service or BroadcastReceiver that is used by your chosen push provider.
     * @return a [String] value, or `null`.
     */
    @JvmStatic
    fun getTitleFromApptentivePush(bundle: Bundle?): String? {
        try {
            when {
                !registered || bundle == null -> return null
                bundle.containsKey(NotificationUtils.TITLE_DEFAULT) -> {
                    return bundle.getString(NotificationUtils.TITLE_DEFAULT)
                }
                bundle.containsKey(NotificationUtils.PUSH_EXTRA_KEY_PARSE) -> {
                    val parseDataString = bundle.getString(NotificationUtils.PUSH_EXTRA_KEY_PARSE)
                    if (parseDataString != null) {
                        return try {
                            val parseJson = JSONObject(parseDataString)
                            parseJson.optString(NotificationUtils.TITLE_DEFAULT)
                        } catch (e: JSONException) {
                            Log.e(PUSH_NOTIFICATION, "Error parsing intent data", e)
                            null
                        }
                    }
                }
                bundle.containsKey(NotificationUtils.PUSH_EXTRA_KEY_UA) -> {
                    val uaPushBundle = bundle.getBundle(NotificationUtils.PUSH_EXTRA_KEY_UA) ?: return null
                    return uaPushBundle.getString(NotificationUtils.TITLE_DEFAULT)
                }
            }
        } catch (exception: Exception) {
            Log.e(PUSH_NOTIFICATION, "Exception while getting title from Apptentive push", exception)
        }
        return null
    }

    /**
     * Use this method in your push receiver to get the notification body text you can use to
     * construct a [android.app.Notification] object.
     *
     * @param bundle A [Bundle] containing the Apptentive Push data. Pass in what you receive in
     * the the Service or BroadcastReceiver that is used by your chosen push provider.
     * @return a [String] value, or `null`.
     */
    @JvmStatic
    fun getBodyFromApptentivePush(bundle: Bundle?): String? {
        try {
            when {
                !registered || bundle == null -> return null
                bundle.containsKey(NotificationUtils.BODY_DEFAULT) -> {
                    return bundle.getString(NotificationUtils.BODY_DEFAULT)
                }
                else -> when {
                    bundle.containsKey(NotificationUtils.PUSH_EXTRA_KEY_PARSE) -> {
                        val parseDataString = bundle.getString(NotificationUtils.PUSH_EXTRA_KEY_PARSE)
                        if (parseDataString != null) {
                            return try {
                                val parseJson = JSONObject(parseDataString)
                                parseJson.optString(NotificationUtils.BODY_PARSE)
                            } catch (e: JSONException) {
                                Log.e(
                                    PUSH_NOTIFICATION,
                                    "Exception while parsing Push Notification",
                                    e
                                )
                                null
                            }
                        }
                    }
                    bundle.containsKey(NotificationUtils.PUSH_EXTRA_KEY_UA) -> {
                        val uaPushBundle = bundle.getBundle(NotificationUtils.PUSH_EXTRA_KEY_UA) ?: return null
                        return uaPushBundle.getString(NotificationUtils.BODY_UA)
                    }
                    bundle.containsKey(NotificationUtils.BODY_UA) -> return bundle.getString(
                        NotificationUtils.BODY_UA
                    )
                }
            }
        } catch (exception: Exception) {
            Log.e(PUSH_NOTIFICATION, "Exception while getting body from Apptentive push", exception)
        }
        return null
    }

    /**
     * Use this method in your push receiver to get the notification title you can use to construct a
     * [android.app.Notification] object.
     *
     * @param data A [Map]<[String], [String]> containing the Apptentive Push
     * data. Pass in what you receive in the the Service or BroadcastReceiver that is
     * used by your chosen push provider.
     * @return a [String] value, or `null`.
     */
    @JvmStatic
    fun getTitleFromApptentivePush(data: Map<String, String>?): String? {
        try {
            return if (!registered) null else data?.get(NotificationUtils.TITLE_DEFAULT)
        } catch (exception: Exception) {
            Log.e(PUSH_NOTIFICATION, "Exception while getting title from Apptentive push", exception)
        }
        return null
    }

    /**
     * Use this method in your push receiver to get the notification body text you can use to
     * construct a [android.app.Notification] object.
     *
     * @param data A [Map]<[String], [String]> containing the Apptentive Push
     * data. Pass in what you receive in the the Service or BroadcastReceiver that is
     * used by your chosen push provider.
     * @return a [String] value, or `null`.
     */
    @JvmStatic
    fun getBodyFromApptentivePush(data: Map<String, String>?): String? {
        try {
            return if (!registered) null else data?.get(NotificationUtils.BODY_DEFAULT)
        } catch (exception: Exception) {
            Log.e(PUSH_NOTIFICATION, "Exception while getting body from Apptentive push", exception)
        }
        return null
    }

    //endregion

    // internal

    @InternalUseOnly
    fun setLocalManifest(json: String?): Boolean {
        return try {
            if (!json.isNullOrBlank()) {
                stateExecutor.execute {
                    client.setLocalManifest(json)
                }
                true
            } else {
                Log.d(CONVERSATION, "json String was null or blank. URI: $json")
                false
            }
        } catch (exception: Exception) {
            Log.e(CONVERSATION, "Exception while setting local manifest as string", exception)
            false
        }
    }

    //endregion
}
