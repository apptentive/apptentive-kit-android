package apptentive.com.android.feedback

import android.content.Context
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
import apptentive.com.android.encryption.NoEncryptionStatus
import apptentive.com.android.encryption.NotEncrypted
import apptentive.com.android.encryption.getEncryptionStatus
import apptentive.com.android.feedback.Apptentive.messageCenterNotificationSubject
import apptentive.com.android.feedback.backend.ConversationPayloadService
import apptentive.com.android.feedback.backend.ConversationService
import apptentive.com.android.feedback.backend.DefaultConversationService
import apptentive.com.android.feedback.backend.MessageCenterService
import apptentive.com.android.feedback.conversation.ConversationManager
import apptentive.com.android.feedback.conversation.ConversationRepository
import apptentive.com.android.feedback.conversation.ConversationSerializer
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
import apptentive.com.android.feedback.model.Conversation
import apptentive.com.android.feedback.model.CustomData
import apptentive.com.android.feedback.model.IntegrationConfig
import apptentive.com.android.feedback.model.IntegrationConfigItem
import apptentive.com.android.feedback.model.MessageCenterNotification
import apptentive.com.android.feedback.model.payloads.AppReleaseAndSDKPayload
import apptentive.com.android.feedback.model.payloads.EventPayload
import apptentive.com.android.feedback.model.payloads.ExtendedData
import apptentive.com.android.feedback.notifications.NotificationUtils
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
import apptentive.com.android.feedback.utils.FileUtil
import apptentive.com.android.feedback.utils.RuntimeUtils
import apptentive.com.android.network.HttpClient
import apptentive.com.android.network.UnexpectedResponseException
import apptentive.com.android.platform.AndroidSharedPrefDataStore
import apptentive.com.android.platform.SharedPrefConstants.CRYPTO_ENABLED
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
import java.io.File
import java.io.InputStream

@InternalUseOnly
class ApptentiveDefaultClient(
    private val configuration: ApptentiveConfiguration,
    private val httpClient: HttpClient,
    private val executors: Executors
) : ApptentiveClient {
    internal lateinit var conversationManager: ConversationManager
    internal lateinit var payloadSender: PayloadSender
    private lateinit var interactionDataProvider: InteractionDataProvider
    private lateinit var interactionModules: Map<String, InteractionModule<Interaction>>
    internal var messageManager: MessageManager? = null
    private var engagement: Engagement = NullEngagement()
    private var encryption: Encryption = setInitialEncryptionFromPastSession()
    private var clearPayloadCache: Boolean = false
    private var clearMessageCache: Boolean = false

    //region Initialization

    @WorkerThread
    internal fun start(context: Context, registerCallback: ((result: RegisterResult) -> Unit)?) {
        interactionModules = loadInteractionModules()

        val conversationService = createConversationService()
        conversationManager = ConversationManager(
            conversationRepository = createConversationRepository(context),
            conversationService = conversationService,
            legacyConversationManagerProvider = object : Provider<LegacyConversationManager> {
                override fun get() = DefaultLegacyConversationManager(context)
            },
            isDebuggable = RuntimeUtils.getApplicationInfo(context).debuggable
        )

        finalizeEncryptionFromConfiguration()

        val serialPayloadSender = SerialPayloadSender(
            payloadQueue = PersistentPayloadQueue.create(context, encryption, clearPayloadCache),
            callback = ::onPayloadSendFinish
        )
        clearPayloadCache = false

        payloadSender = serialPayloadSender

        getConversationToken(conversationService, registerCallback)
        addObservers(serialPayloadSender, conversationService)
    }

    @WorkerThread
    private fun getConversationToken(
        conversationService: ConversationService,
        registerCallback: ((result: RegisterResult) -> Unit)?
    ) {
        conversationManager.fetchConversationToken { result ->
            when (result) {
                is Result.Error -> {
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
                    val activeConversation = conversationManager.activeConversation.value
                    if (activeConversation.conversationId != null && activeConversation.conversationToken != null) {
                        registerCallback?.invoke(RegisterResult.Success)
                    }
                    messageManager = MessageManager(
                        activeConversation.conversationId,
                        activeConversation.conversationToken,
                        conversationService as MessageCenterService,
                        executors.state,
                        DefaultMessageRepository(
                            messageSerializer = DefaultMessageSerializer(messagesFile = getMessagesFile(), encryption).apply {
                                if (clearMessageCache) {
                                    deleteAllMessages()
                                    clearMessageCache = false
                                }
                            }
                        )
                    )
                    messageManager?.let {
                        DependencyProvider.register(MessageManagerFactoryProvider(it))
                        it.addUnreadMessageListener(::updateMessageCenterNotification)
                    }
                }
            }
        }
    }

    private fun addObservers(serialPayloadSender: SerialPayloadSender, conversationService: ConversationService) {
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
            // register engagement context as soon as DefaultEngagement is created to make it available for MessageManager
            DependencyProvider.register(EngagementContextProvider(engagement, payloadSender, executors))
            // once we have received conversationId and conversationToken we can setup payload sender service
            val conversationId = conversation.conversationId
            val conversationToken = conversation.conversationToken
            if (conversationId != null && conversationToken != null && !serialPayloadSender.hasPayloadService) {
                serialPayloadSender.setPayloadService(
                    service = ConversationPayloadService(
                        requestSender = conversationService,
                        conversationId = conversationId,
                        conversationToken = conversationToken
                    )
                )
            }
            messageManager?.onConversationChanged(conversation)
            updateMessageCenterNotification()
        }
        // add an observer to track SDK & AppRelease changes
        conversationManager.sdkAppReleaseUpdate.observe { appReleaseSDKUpdated ->
            if (appReleaseSDKUpdated) {
                val sdk = conversationManager.getConversation().sdk
                val appRelease = conversationManager.getConversation().appRelease
                val payload = AppReleaseAndSDKPayload.buildPayload(sdk = sdk, appRelease = appRelease)
                payloadSender.sendPayload(payload)
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
            conversationFile = getConversationFile(),
            manifestFile = getManifestFile(),
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
        return if (this::interactionDataProvider.isInitialized) { // Check if lateinit value is set
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
            payloadSender.sendPayload(newPerson.toPersonPayload())
        }
    }

    override fun updateMParticleID(id: String) {
        val person = conversationManager.getConversation().person
        val newPerson = person.copy(mParticleId = id)
        if (person != newPerson) {
            conversationManager.updatePerson(newPerson)
            payloadSender.sendPayload(newPerson.toPersonPayload())
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
        return if (this::interactionDataProvider.isInitialized) { // Check if lateinit value is set
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
        payloadSender.sendPayload(device.toDevicePayload())
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
            payloadSender.sendPayload(newDevice.toDevicePayload())
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
        payloadSender.sendPayload(
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
                    val EVENT_NAME_MESSAGE_HTTP_ERROR = "message_http_error"
                    engage(Event.internal(EVENT_NAME_MESSAGE_HTTP_ERROR, InteractionType.MessageCenter))
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

    private fun finalizeEncryptionFromConfiguration() {
        if ((configuration.shouldEncryptStorage && (encryption is EncryptionNoOp)) ||
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

        clearMessageCache = true
        clearPayloadCache = true
    }

    fun getOldEncryptionSetting(): EncryptionStatus {
        val sharedPref = DependencyProvider.of<AndroidSharedPrefDataStore>()

        return when {
            FileUtil.containsFiles(CONVERSATION_DIR) && !sharedPref.containsKey(SDK_CORE_INFO, CRYPTO_ENABLED) -> NotEncrypted // Migrating from 6.0.0
            sharedPref.containsKey(SDK_CORE_INFO, CRYPTO_ENABLED) -> sharedPref.getBoolean(SDK_CORE_INFO, CRYPTO_ENABLED).getEncryptionStatus()
            else -> NoEncryptionStatus
        }
    }

    //endregion

    internal fun getConversationId() = conversationManager.getConversation().conversationId

    companion object {
        val sessionId = generateUUID()

        private const val CONVERSATION_DIR = "conversations"

        @WorkerThread
        private fun getConversationFile(): File {
            val conversationsDir = getConversationDir()
            return File(conversationsDir, "conversation.bin")
        }

        @WorkerThread
        private fun getManifestFile(): File {
            val conversationsDir = getConversationDir()
            return File(conversationsDir, "manifest.bin")
        }

        @WorkerThread
        private fun getConversationDir(): File {
            return FileUtil.getInternalDir(CONVERSATION_DIR, createIfNecessary = true)
        }

        @WorkerThread
        private fun getMessagesFile(): File {
            val conversationsDir = getConversationDir()
            return File(conversationsDir, "messages.bin")
        }
    }
}
