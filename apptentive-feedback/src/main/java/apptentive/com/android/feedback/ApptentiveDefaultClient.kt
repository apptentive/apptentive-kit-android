package apptentive.com.android.feedback

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.WorkerThread
import androidx.lifecycle.ProcessLifecycleOwner
import apptentive.com.android.concurrent.Executors
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.core.Provider
import apptentive.com.android.encryption.AESEncryption23
import apptentive.com.android.encryption.Encryption
import apptentive.com.android.encryption.EncryptionFactory
import apptentive.com.android.encryption.EncryptionNoOp
import apptentive.com.android.encryption.EncryptionStatus
import apptentive.com.android.encryption.KeyResolverFactory
import apptentive.com.android.encryption.NoEncryptionStatus
import apptentive.com.android.encryption.NotEncrypted
import apptentive.com.android.encryption.getEncryptionStatus
import apptentive.com.android.feedback.Apptentive.executeCallbackInMainExecutor
import apptentive.com.android.feedback.Apptentive.messageCenterNotificationSubject
import apptentive.com.android.feedback.backend.ConversationPayloadService
import apptentive.com.android.feedback.backend.ConversationService
import apptentive.com.android.feedback.backend.DefaultConversationService
import apptentive.com.android.feedback.backend.MessageCenterService
import apptentive.com.android.feedback.conversation.ConversationCredential
import apptentive.com.android.feedback.conversation.ConversationCredentialProvider
import apptentive.com.android.feedback.conversation.ConversationManager
import apptentive.com.android.feedback.conversation.ConversationMetaData
import apptentive.com.android.feedback.conversation.ConversationRepository
import apptentive.com.android.feedback.conversation.ConversationSerializer
import apptentive.com.android.feedback.conversation.ConversationState
import apptentive.com.android.feedback.conversation.DefaultConversationRepository
import apptentive.com.android.feedback.conversation.DefaultConversationSerializer
import apptentive.com.android.feedback.engagement.DefaultEngagement
import apptentive.com.android.feedback.engagement.DefaultInteractionEngagement
import apptentive.com.android.feedback.engagement.Engagement
import apptentive.com.android.feedback.engagement.EngagementContextFactory
import apptentive.com.android.feedback.engagement.EngagementContextProvider
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.InteractionDataProvider
import apptentive.com.android.feedback.engagement.InteractionEngagement
import apptentive.com.android.feedback.engagement.InternalEvent
import apptentive.com.android.feedback.engagement.NullEngagement
import apptentive.com.android.feedback.engagement.criteria.CachedInvocationProvider
import apptentive.com.android.feedback.engagement.criteria.CriteriaInteractionDataProvider
import apptentive.com.android.feedback.engagement.criteria.DefaultTargetingState
import apptentive.com.android.feedback.engagement.criteria.InvocationConverter
import apptentive.com.android.feedback.engagement.interactions.DefaultInteractionDataConverter
import apptentive.com.android.feedback.engagement.interactions.Interaction
import apptentive.com.android.feedback.engagement.interactions.InteractionDataConverter
import apptentive.com.android.feedback.engagement.interactions.InteractionLauncher
import apptentive.com.android.feedback.engagement.interactions.InteractionModule
import apptentive.com.android.feedback.engagement.interactions.InteractionResponse
import apptentive.com.android.feedback.engagement.interactions.InteractionType
import apptentive.com.android.feedback.lifecycle.ApptentiveLifecycleObserver
import apptentive.com.android.feedback.message.DefaultMessageRepository
import apptentive.com.android.feedback.message.DefaultMessageSerializer
import apptentive.com.android.feedback.message.EVENT_MESSAGE_CENTER
import apptentive.com.android.feedback.message.MessageManager
import apptentive.com.android.feedback.message.MessageManagerFactoryProvider
import apptentive.com.android.feedback.message.MessageRepository
import apptentive.com.android.feedback.model.Conversation
import apptentive.com.android.feedback.model.CustomData
import apptentive.com.android.feedback.model.IntegrationConfig
import apptentive.com.android.feedback.model.IntegrationConfigItem
import apptentive.com.android.feedback.model.MessageCenterNotification
import apptentive.com.android.feedback.model.payloads.AppReleaseAndSDKPayload
import apptentive.com.android.feedback.model.payloads.EventPayload
import apptentive.com.android.feedback.model.payloads.ExtendedData
import apptentive.com.android.feedback.model.payloads.LogoutPayload
import apptentive.com.android.feedback.model.payloads.Payload
import apptentive.com.android.feedback.notifications.NotificationUtils
import apptentive.com.android.feedback.payload.AuthenticationFailureException
import apptentive.com.android.feedback.payload.PayloadData
import apptentive.com.android.feedback.payload.PayloadSender
import apptentive.com.android.feedback.payload.PayloadType
import apptentive.com.android.feedback.payload.PersistentPayloadQueue
import apptentive.com.android.feedback.payload.SerialPayloadSender
import apptentive.com.android.feedback.platform.DefaultAppReleaseFactory
import apptentive.com.android.feedback.platform.DefaultDeviceFactory
import apptentive.com.android.feedback.platform.DefaultEngagementDataFactory
import apptentive.com.android.feedback.platform.DefaultEngagementManifestFactory
import apptentive.com.android.feedback.platform.DefaultPersonFactory
import apptentive.com.android.feedback.platform.DefaultSDKFactory
import apptentive.com.android.feedback.platform.DefaultStateMachine
import apptentive.com.android.feedback.platform.SDKEvent
import apptentive.com.android.feedback.utils.FileStorageUtil
import apptentive.com.android.feedback.utils.FileStorageUtil.deleteMessageFile
import apptentive.com.android.feedback.utils.FileUtil
import apptentive.com.android.feedback.utils.JwtString
import apptentive.com.android.feedback.utils.JwtUtils
import apptentive.com.android.feedback.utils.RuntimeUtils
import apptentive.com.android.feedback.utils.getActiveConversationMetaData
import apptentive.com.android.feedback.utils.toEncryptionKey
import apptentive.com.android.network.HttpClient
import apptentive.com.android.network.UnexpectedResponseException
import apptentive.com.android.platform.AndroidSharedPrefDataStore
import apptentive.com.android.platform.SharedPrefConstants.APPTENTIVE
import apptentive.com.android.platform.SharedPrefConstants.CRYPTO_ENABLED
import apptentive.com.android.platform.SharedPrefConstants.PREF_KEY_PUSH_PROVIDER
import apptentive.com.android.platform.SharedPrefConstants.PREF_KEY_PUSH_TOKEN
import apptentive.com.android.platform.SharedPrefConstants.SDK_CORE_INFO
import apptentive.com.android.util.InternalUseOnly
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogLevel
import apptentive.com.android.util.LogTags.CONVERSATION
import apptentive.com.android.util.LogTags.CRYPTOGRAPHY
import apptentive.com.android.util.LogTags.EVENT
import apptentive.com.android.util.LogTags.LIFE_CYCLE_OBSERVER
import apptentive.com.android.util.LogTags.MESSAGE_CENTER
import apptentive.com.android.util.LogTags.PAYLOADS
import apptentive.com.android.util.LogTags.PUSH_NOTIFICATION
import apptentive.com.android.util.Result
import apptentive.com.android.util.generateUUID
import com.apptentive.android.sdk.conversation.DefaultLegacyConversationManager
import com.apptentive.android.sdk.conversation.LegacyConversationManager
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.lang.ref.WeakReference

@InternalUseOnly
class ApptentiveDefaultClient(
    internal val configuration: ApptentiveConfiguration,
    private val httpClient: HttpClient,
    private val executors: Executors,
) : ApptentiveClient {
    internal lateinit var conversationManager: ConversationManager
    internal lateinit var payloadSender: PayloadSender
    private lateinit var interactionDataProvider: InteractionDataProvider
    private lateinit var interactionModules: Map<String, InteractionModule<Interaction>>
    private lateinit var conversationService: ConversationService
    internal var messageManager: MessageManager? = null
    private var engagement: Engagement = NullEngagement()
    private var encryption: Encryption = setInitialEncryptionFromPastSession()
    private var clearPayloadCache: Boolean = false
    private var authenticationFailedListener: WeakReference<AuthenticationFailedListener>? = null

    //region Initialization

    internal fun initialize(context: Context) {
        // Consider moving other parameters out of the constructor as well
        interactionModules = loadInteractionModules()
        conversationService = createConversationService()
        DependencyProvider.register(createConversationRepository(context))
    }

    @WorkerThread
    internal fun start(context: Context, registerCallback: ((result: RegisterResult) -> Unit)?) {
        DefaultStateMachine.onEvent(SDKEvent.ClientStarted)
        conversationManager = ConversationManager(
            conversationRepository = DependencyProvider.of(),
            conversationService = conversationService,
            legacyConversationManagerProvider = object : Provider<LegacyConversationManager> {
                override fun get() = DefaultLegacyConversationManager(context)
            },
            isDebuggable = RuntimeUtils.getApplicationInfo(context).debuggable
        )

        finalizeEncryption()

        payloadSender = SerialPayloadSender(
            payloadQueue = PersistentPayloadQueue.create(context, encryption, clearPayloadCache),
            callback = ::onPayloadSendFinish
        )
        clearPayloadCache = false

        getConversationToken(registerCallback)
        addObservers(conversationService)

        engage(Event.internal(InternalEvent.APP_LAUNCH.labelName))
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @WorkerThread
    private fun getConversationToken(
        registerCallback: ((result: RegisterResult) -> Unit)?
    ) {
        conversationManager.tryFetchConversationToken { result ->
            when (result) {
                is Result.Error -> {
                    DefaultStateMachine.onEvent(SDKEvent.Error)
                    when (val error = result.error) {
                        is UnexpectedResponseException -> {
                            val responseCode = error.statusCode
                            val message = error.errorMessage
                            registerCallback?.invoke(
                                RegisterResult.Failure(
                                    message ?: "Failed to fetch conversation token", responseCode
                                )
                            )
                        }
                        else -> registerCallback?.invoke(RegisterResult.Exception(result.error))
                    }
                }
                is Result.Success -> {
                    conversationManager.tryFetchEngagementManifest()
                    conversationManager.tryFetchAppConfiguration()
                    createMessageManager()
                    registerCallback?.invoke(RegisterResult.Success)
                }
            }
        }
    }

    private fun addObservers(conversationService: ConversationService) {
        conversationManager.activeConversation.observe { conversation ->
            if (Log.canLog(LogLevel.Verbose)) { // avoid unnecessary computations
                conversation.logConversation()
            }

            interactionDataProvider = createInteractionDataProvider(conversation)

            engagement = DefaultEngagement(
                interactionDataProvider = interactionDataProvider,
                interactionConverter = interactionConverter,
                interactionEngagement = createInteractionEngagement(),
                recordEvent = ::recordEvent,
                recordInteraction = ::recordInteraction,
                recordInteractionResponses = ::recordInteractionResponses,
                recordCurrentAnswer = ::recordCurrentAnswer
            )
            // once we have received conversationId and conversationToken we can setup payload sender service
            val conversationId = conversation.conversationId
            val conversationToken = conversation.conversationToken
            if (conversationId != null && conversationToken != null && !(payloadSender as SerialPayloadSender).hasPayloadService) {
                (payloadSender as SerialPayloadSender).setPayloadService(
                    service = ConversationPayloadService(
                        requestSender = conversationService,
                    )
                )
            }
            // register engagement context as soon as DefaultEngagement is created to make it available for MessageManager
            DependencyProvider.register(EngagementContextProvider(engagement, payloadSender, executors))
            messageManager?.onConversationChanged(conversation)
            updateMessageCenterNotification()
        }
        // add an observer to track SDK & AppRelease changes
        conversationManager.sdkAppReleaseUpdate.observe { appReleaseSDKUpdated ->
            if (appReleaseSDKUpdated) {
                val sdk = conversationManager.getConversation().sdk
                val appRelease = conversationManager.getConversation().appRelease
                val payload = AppReleaseAndSDKPayload.buildPayload(sdk = sdk, appRelease = appRelease)
                enqueuePayload(payload)
            }
        }

        executors.main.execute {
            Log.i(LIFE_CYCLE_OBSERVER, "Observing App lifecycle")
            ProcessLifecycleOwner.get().lifecycle.addObserver(
                ApptentiveLifecycleObserver(
                    client = this,
                    stateExecutor = executors.state,
                    onForeground = {
                        conversationManager.tryFetchEngagementManifest()
                        conversationManager.tryFetchAppConfiguration()
                        messageManager?.onAppForeground()
                    },
                    onBackground = {
                        messageManager?.onAppBackground()
                    }
                )
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun login(jwtToken: JwtString, callback: ((result: LoginResult) -> Unit)?) {
        val activeConversationMetaData = getActiveConversationMetaData()
        val subClaim = JwtUtils.extractSub(jwtToken)

        if (subClaim == null) {
            callback?.invoke(LoginResult.Error("Invalid JWT token"))
            return
        }

        when {
            activeConversationMetaData == null -> {
                Log.v(CONVERSATION, "No active conversation found")
                handleNoActiveConversation(subClaim, jwtToken, callback)
            }
            activeConversationMetaData.state is ConversationState.Anonymous -> {
                Log.v(CONVERSATION, "Active conversation is anonymous")
                loginAnonymousConversation(jwtToken, subClaim, callback)
            }
            activeConversationMetaData.state is ConversationState.LoggedIn -> {
                Log.v(CONVERSATION, "Already logged in. Logout before calling login")
                executeCallbackInMainExecutor(callback, LoginResult.Error("Already logged in. Logout before calling login"))
            }
            else -> {
                Log.v(CONVERSATION, "Cannot login while SDK is in ${activeConversationMetaData.state}")
                executeCallbackInMainExecutor(callback, LoginResult.Error("Cannot login while SDK is in ${activeConversationMetaData.state}"))
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun handleNoActiveConversation(subClaim: JwtString, jwtToken: String, callback: ((result: LoginResult) -> Unit)?) {
        val matchingMetaData = findMatchingMetaData(subClaim)
        val conversationId = (matchingMetaData?.state as? ConversationState.LoggedOut)?.id

        if (conversationId != null) {
            Log.v(CONVERSATION, "Found matching conversation ID in logged out list")
            val legacyConversationPath = if (FileUtil.isConversationCacheStoredInLegacyFormat(matchingMetaData.path)) {
                Log.v(CONVERSATION, "Conversation cache is in legacy format.")
                matchingMetaData.path
            } else null
            conversationManager.loginSession(conversationId, jwtToken, subClaim, legacyConversationPath) { result ->
                handleLoginResult(result, callback)
            }
        } else {
            conversationManager.createConversationAndLogin(jwtToken, subClaim) { result ->
                handleLoginResult(result, callback)
            }
        }
    }

    private fun findMatchingMetaData(subClaim: JwtString): ConversationMetaData? {
        val matchingMetaData = DefaultStateMachine.conversationRoster.loggedOut.firstOrNull {
            it.state is ConversationState.LoggedOut && (it.state as ConversationState.LoggedOut).subject == subClaim
        }
        return matchingMetaData
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun loginAnonymousConversation(jwtToken: JwtString, subject: String, loginCallback: ((result: LoginResult) -> Unit)? = null) {
        val conversationId = conversationManager.getConversation().conversationId
        conversationId?.let { id ->
            conversationManager.loginSession(id, jwtToken, subject) { result ->
                handleLoginResult(result, loginCallback)
            }
        }
    }

    private fun handleLoginResult(result: LoginResult, callback: ((result: LoginResult) -> Unit)?) {
        when (result) {
            is LoginResult.Success -> {
                Log.v(CONVERSATION, "Successfully logged in")
                engage(Event.internal(InternalEvent.SDK_LOGIN.labelName))
                if (messageManager == null) {
                    createMessageManager()
                }
                messageManager?.login()
                messageManager?.addUnreadMessageListener(::updateMessageCenterNotification)
                val sharedPrefDataStore = DependencyProvider.of<AndroidSharedPrefDataStore>()
                val pushProvider = sharedPrefDataStore.getInt(APPTENTIVE, PREF_KEY_PUSH_PROVIDER)
                val pushProviderName = sharedPrefDataStore.getString(APPTENTIVE, PREF_KEY_PUSH_TOKEN)
                setPushIntegration(pushProvider, pushProviderName)
                executeCallbackInMainExecutor(callback, LoginResult.Success)
            }
            is LoginResult.Error -> {
                Log.v(CONVERSATION, "Failed to login")
                executeCallbackInMainExecutor(callback, LoginResult.Error("Failed to login"))
            }
            is LoginResult.Failure -> {
                Log.v(CONVERSATION, "Failed to login")
                executeCallbackInMainExecutor(callback, LoginResult.Failure("Failed to login", result.responseCode))
            }
            is LoginResult.Exception -> {
                Log.v(CONVERSATION, "Failed to login")
                executeCallbackInMainExecutor(callback, LoginResult.Exception(result.error))
            }
        }
    }

    private fun createMessageManager() {
        if (!DependencyProvider.isRegistered<MessageRepository>()) {
            val messageRepository = DefaultMessageRepository(
                messageSerializer = DefaultMessageSerializer(
                    encryption,
                    DefaultStateMachine.conversationRoster
                )
            )
            DependencyProvider.register(messageRepository as MessageRepository)
            Log.d(CONVERSATION, "MessageRepository registered")
        }
        messageManager = MessageManager(
            conversationService as MessageCenterService,
            executors.state,
            DependencyProvider.of<MessageRepository>(),
        )
        messageManager?.let {
            DependencyProvider.register(MessageManagerFactoryProvider(it))
            it.addUnreadMessageListener(::updateMessageCenterNotification)
        }
    }

    override fun logout() {
        conversationManager.logoutSession {
            if (it is Result.Success) {
                engage(Event.internal(InternalEvent.SDK_LOGOUT.labelName))
                enqueuePayload(LogoutPayload())
                conversationManager.setManifestExpired()
                messageManager?.logout()
            }
        }
    }

    override fun setAuthenticationFailedListener(listener: AuthenticationFailedListener) {
        authenticationFailedListener = WeakReference(listener)
    }

    override fun updateToken(jwtToken: JwtString, callback: ((result: LoginResult) -> Unit)?) {
        conversationManager.updateToken(jwtToken, callback)
        val conversationCredentialProvider = if (DependencyProvider.isRegistered<ConversationCredentialProvider>()) {
            DependencyProvider.of<ConversationCredentialProvider>()
        } else ConversationCredential()
        payloadSender.updateCredential(conversationCredentialProvider, conversationCredentialProvider.conversationPath)
    }

    @WorkerThread
    private fun createConversationRepository(context: Context): ConversationRepository {
        return DefaultConversationRepository(
            conversationSerializer = createConversationSerializer(),
            appReleaseFactory = DefaultAppReleaseFactory(context),
            personFactory = DefaultPersonFactory(),
            deviceFactory = DefaultDeviceFactory(context),
            sdkFactory = DefaultSDKFactory(
                version = Constants.SDK_VERSION,
                distribution = configuration.distributionName,
                distributionVersion = configuration.distributionVersion
            ),
            manifestFactory = DefaultEngagementManifestFactory(),
            engagementDataFactory = DefaultEngagementDataFactory()
        )
    }

    private fun createConversationSerializer(): ConversationSerializer {
        return DefaultConversationSerializer(
            conversationRosterFile = FileStorageUtil.getRosterFile(configuration.apptentiveKey),
        ).apply {
            setEncryption(encryption)
        }
    }

    private fun createConversationService(): ConversationService = DefaultConversationService(
        httpClient = httpClient,
        apptentiveKey = configuration.apptentiveKey,
        apptentiveSignature = configuration.apptentiveSignature,
        apiVersion = Constants.API_VERSION,
        sdkVersion = Constants.SDK_VERSION,
        baseURL = Constants.SERVER_URL
    )

    private fun createInteractionDataProvider(conversation: Conversation): InteractionDataProvider {
        val interactions = conversation.engagementManifest.interactions.map { it.id to it }.toMap()
        val usingCustomStoreUrlSkipInAppReviewID = if (conversation.appRelease.customAppStoreURL != null) {
            interactions.entries.find {
                it.value.type == InteractionType.GoogleInAppReview.name
            }?.key
        } else null

        return CriteriaInteractionDataProvider(
            interactions = interactions,
            invocationProvider = CachedInvocationProvider(
                conversation.engagementManifest.targets,
                InvocationConverter
            ),
            state = DefaultTargetingState(
                conversation.person,
                conversation.device,
                conversation.sdk,
                conversation.appRelease,
                conversation.randomSampling,
                conversation.engagementData
            ),
            usingCustomStoreUrlSkipInAppReviewID = usingCustomStoreUrlSkipInAppReviewID
        )
    }

    //endregion

    //region Engagement

    override fun engage(event: Event, customData: Map<String, Any?>?): EngagementResult {
        return DependencyProvider.of<EngagementContextFactory>().engagementContext().engage(
            event = event,
            customData = filterCustomData(customData)
        )
    }

    override fun canShowInteraction(event: Event): Boolean {
        return if (this::interactionDataProvider.isInitialized) {
            interactionDataProvider.getInteractionData(event) != null
        } else false
    }

    //region Person
    override fun updatePerson(
        name: String?,
        email: String?,
        customData: Pair<String, Any?>?,
        deleteKey: String?
    ) {
        val person = conversationManager.getConversation().person
        val newPerson = when {
            name != null -> person.copy(name = name)
            email != null -> person.copy(email = email)
            customData != null -> {
                val newContent = person.customData.content.plus(customData)
                person.copy(customData = CustomData(newContent))
            }
            deleteKey != null -> {
                val newContent = person.customData.content.minus(deleteKey)
                person.copy(customData = CustomData(newContent))
            }
            else -> person
        }
        if (person != newPerson) {
            conversationManager.updatePerson(newPerson)
            enqueuePayload(newPerson.toPersonPayload())
        }
    }

    override fun updateMParticleID(id: String) {
        val person = conversationManager.getConversation().person
        val newPerson = person.copy(mParticleId = id)
        if (person != newPerson) {
            conversationManager.updatePerson(newPerson)
            enqueuePayload(newPerson.toPersonPayload())
        }
    }

    override fun getPersonName(): String? {
        return conversationManager.getConversation().person.name
    }

    override fun getPersonEmail(): String? {
        return conversationManager.getConversation().person.email
    }
    //endregion

    //region Message Center
    override fun showMessageCenter(customData: Map<String, Any?>?): EngagementResult {
        filterCustomData(customData)?.let { filteredCustomData ->
            messageManager?.setCustomData(filteredCustomData)
        }
        return engage(Event.internal(EVENT_MESSAGE_CENTER))
    }

    override fun getUnreadMessageCount(): Int {
        return messageManager?.getUnreadMessageCount() ?: 0
    }

    override fun canShowMessageCenter(): Boolean {
        return if (this::interactionDataProvider.isInitialized) {
            interactionDataProvider.getInteractionData(Event.internal(EVENT_MESSAGE_CENTER)) != null
        } else false
    }

    internal fun updateMessageCenterNotification() {
        val notification = MessageCenterNotification(
            canShowMessageCenter = canShowMessageCenter(),
            unreadMessageCount = getUnreadMessageCount(),
            personName = getPersonName(),
            personEmail = getPersonEmail()
        )

        // Only update if something has changed
        if (notification != messageCenterNotificationSubject.value) {
            messageCenterNotificationSubject.value = notification
        }
    }

    override fun sendHiddenTextMessage(message: String) {
        messageManager?.sendMessage(message, isHidden = true)
    }

    override fun sendHiddenAttachmentFileUri(uri: String) {
        messageManager?.sendAttachment(uri, true)
    }

    override fun sendHiddenAttachmentFileBytes(bytes: ByteArray, mimeType: String) {
        var inputStream: ByteArrayInputStream? = null
        try {
            inputStream = ByteArrayInputStream(bytes)
            messageManager?.sendHiddenAttachmentFromInputStream(inputStream, mimeType)
        } catch (e: Exception) {
            Log.e(MESSAGE_CENTER, "Exception when sending attachment. Closing input stream.", e)
        } finally {
            FileUtil.ensureClosed(inputStream)
        }
    }

    override fun sendHiddenAttachmentFileStream(inputStream: InputStream, mimeType: String) {
        messageManager?.sendHiddenAttachmentFromInputStream(inputStream, mimeType)
    }
    //endregion

    private fun filterCustomData(customData: Map<String, Any?>?): Map<String, Any?>? {
        if (customData == null) return null // No custom data set

        val filteredContent = customData.filter {
            it.value is String || it.value is Number || it.value is Boolean
        }

        return filteredContent.ifEmpty {
            Log.w(EVENT, "Not setting custom data. No supported types found.")
            null
        }
    }

    override fun setPushIntegration(pushProvider: Int, token: String) {
        Log.d(PUSH_NOTIFICATION, "Setting push provider with token $token")
        val device = conversationManager.getConversation().device
        val integrationConfig: IntegrationConfig = device.integrationConfig
        val item = IntegrationConfigItem(mapOf(NotificationUtils.KEY_TOKEN to token))
        when (pushProvider) {
            Apptentive.PUSH_PROVIDER_APPTENTIVE -> integrationConfig.apptentive = item
            Apptentive.PUSH_PROVIDER_PARSE -> integrationConfig.parse = item
            Apptentive.PUSH_PROVIDER_URBAN_AIRSHIP -> integrationConfig.urbanAirship = item
            Apptentive.PUSH_PROVIDER_AMAZON_AWS_SNS -> integrationConfig.amazonAwsSns = item
            else -> Log.e(CONVERSATION, "Invalid pushProvider: $pushProvider")
        }
        conversationManager.updateDevice(device)
        enqueuePayload(device.toDevicePayload())
    }

    override fun updateDevice(customData: Pair<String, Any?>?, deleteKey: String?) {
        val device = conversationManager.getConversation().device
        val newDevice = when {
            customData != null -> {
                val newContent = device.customData.content.plus(customData)
                device.copy(customData = CustomData(newContent))
            }
            deleteKey != null -> {
                val newContent = device.customData.content.minus(deleteKey)
                device.copy(customData = CustomData(newContent))
            }
            else -> device
        }
        if (device != newDevice) {
            conversationManager.updateDevice(newDevice)
            enqueuePayload(newDevice.toDevicePayload())
        }
    }

    private val interactionLaunchersLookup: Map<Class<Interaction>, InteractionLauncher<Interaction>> by lazy {
        interactionModules.map { (_, module) ->
            Pair(module.interactionClass, module.provideInteractionLauncher())
        }.toMap()
    }

    private val interactionConverter: InteractionDataConverter by lazy {
        DefaultInteractionDataConverter(
            lookup = interactionModules.mapValues { (_, module) ->
                module.provideInteractionTypeConverter()
            }
        )
    }

    private fun createInteractionEngagement(): InteractionEngagement {
        return DefaultInteractionEngagement(lookup = interactionLaunchersLookup)
    }

    private fun loadInteractionModules(): Map<String, InteractionModule<Interaction>> {
        return InteractionModuleComponent.default().getModules()
    }

    @WorkerThread
    private fun recordEvent(
        event: Event,
        interactionId: String?,
        data: Map<String, Any?>?,
        customData: Map<String, Any?>?,
        extendedData: List<ExtendedData>?
    ) {
        // store event locally
        conversationManager.recordEvent(event)

        // send event to the backend
        enqueuePayload(
            EventPayload(
                label = event.fullName,
                interactionId = interactionId,
                data = data,
                customData = customData,
                extendedData = extendedData
            )
        )
    }

    override fun setLocalManifest(json: String) {
        conversationManager.setTestManifestFromLocal(json)
    }

    @WorkerThread
    private fun recordInteraction(interaction: Interaction) {
        conversationManager.recordInteraction(interaction.id)
    }

    @WorkerThread
    private fun recordInteractionResponses(interactionResponses: Map<String, Set<InteractionResponse>>) {
        conversationManager.recordInteractionResponses(interactionResponses)
    }

    @WorkerThread
    private fun recordCurrentAnswer(interactionResponses: Map<String, Set<InteractionResponse>>, reset: Boolean) {
        conversationManager.recordCurrentResponse(interactionResponses, reset)
    }

    @WorkerThread
    private fun onPayloadSendFinish(result: Result<PayloadData>) {
        when (result) {
            is Result.Success -> {
                val resultData = result.data

                if (resultData.type == PayloadType.Message) messageManager?.updateMessageStatus(true, resultData)

                Log.d(PAYLOADS, "Payload of type \'${resultData.type}\' successfully sent")
            }
            is Result.Error -> {
                val resultData = result.data as? PayloadData
                if (resultData?.type == PayloadType.Message) {
                    messageManager?.updateMessageStatus(false, resultData)
                    engage(Event.internal(InternalEvent.EVENT_MESSAGE_HTTP_ERROR.labelName, InteractionType.MessageCenter))
                }

                val resultError = result.error as? AuthenticationFailureException

                if (resultError != null) {
                    val reason = AuthenticationFailedReason.parse(resultError.errorType, resultError.errorMessage)
                    authenticationFailedListener?.get()?.onAuthenticationFailed(reason)
                }

                Log.e(PAYLOADS, "Payload failed to send: ${result.error.cause}")
            }
        }
    }

    //endregion

    //region Encryption

    private fun setInitialEncryptionFromPastSession(): Encryption {
        val sharedPref = DependencyProvider.of<AndroidSharedPrefDataStore>()
        val oldEncryptionSetting = getOldEncryptionSetting()
        val encryption = EncryptionFactory.getEncryption(
            shouldEncryptStorage = configuration.shouldEncryptStorage,
            oldEncryptionSetting = oldEncryptionSetting
        )
        sharedPref.putBoolean(SDK_CORE_INFO, CRYPTO_ENABLED, encryption is AESEncryption23)
        Log.d(CRYPTOGRAPHY, "Initial encryption setting is ${encryption.javaClass.simpleName}")
        return encryption
    }

    private fun finalizeEncryption() {
        val activeConversationState = DefaultStateMachine.conversationRoster.activeConversation?.state
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activeConversationState is ConversationState.LoggedIn) {
            val wrapperEncryptionBytes = activeConversationState.encryptionWrapperBytes
            val encryptionKey = KeyResolverFactory.getKeyResolver().resolveMultiUserWrapperKey(activeConversationState.subject)
            AESEncryption23(encryptionKey).decrypt(wrapperEncryptionBytes).let {
                encryption = AESEncryption23(it.toEncryptionKey())
            }
        } else if ((configuration.shouldEncryptStorage && (encryption is EncryptionNoOp)) ||
            (!configuration.shouldEncryptStorage && (encryption is AESEncryption23))
        ) {
            onFinalEncryptionSettingsChanged()
        }
        Log.d(CRYPTOGRAPHY, "Final encryption setting is ${encryption.javaClass.simpleName}")
        conversationManager.onEncryptionSetupComplete()
    }

    private fun onFinalEncryptionSettingsChanged() {
        val sharedPref = DependencyProvider.of<AndroidSharedPrefDataStore>()

        sharedPref.putBoolean(SDK_CORE_INFO, CRYPTO_ENABLED, configuration.shouldEncryptStorage)
        encryption = EncryptionFactory.getEncryption(
            shouldEncryptStorage = configuration.shouldEncryptStorage,
            oldEncryptionSetting = getOldEncryptionSetting()
        )

        conversationManager.updateEncryption(encryption)

        deleteMessageFile() // delete message file to force re-encryption

        clearPayloadCache = true
    }

    fun getOldEncryptionSetting(): EncryptionStatus {
        val sharedPref = DependencyProvider.of<AndroidSharedPrefDataStore>()

        return when {
            // TODO revisit this logic if necessary as the folder structure would change from 6.2
            FileUtil.containsFiles(FileStorageUtil.CONVERSATION_DIR) && !sharedPref.containsKey(SDK_CORE_INFO, CRYPTO_ENABLED) -> NotEncrypted // Migrating from 6.0.0
            sharedPref.containsKey(SDK_CORE_INFO, CRYPTO_ENABLED) -> sharedPref.getBoolean(SDK_CORE_INFO, CRYPTO_ENABLED).getEncryptionStatus()
            else -> NoEncryptionStatus
        }
    }

    //endregion

    internal fun getConversationId() = conversationManager.getConversation().conversationId

    private fun enqueuePayload(payload: Payload) {
        val conversationCredential = if (DependencyProvider.isRegistered<ConversationCredentialProvider>()) {
            DependencyProvider.of<ConversationCredentialProvider>()
        } else ConversationCredential()
        payloadSender.enqueuePayload(payload, conversationCredential)
    }

    companion object {
        // Gets created on the first call to Apptentive.register() and is used to identify the session
        val sessionId = generateUUID()
    }
}
