package apptentive.com.android.feedback

import android.content.Context
import androidx.annotation.WorkerThread
import androidx.lifecycle.ProcessLifecycleOwner
import apptentive.com.android.concurrent.Executors
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.core.Provider
import apptentive.com.android.feedback.backend.ConversationService
import apptentive.com.android.feedback.backend.DefaultConversationService
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
import apptentive.com.android.feedback.model.Conversation
import apptentive.com.android.feedback.model.CustomData
import apptentive.com.android.feedback.model.payloads.AppReleaseAndSDKPayload
import apptentive.com.android.feedback.model.payloads.EventPayload
import apptentive.com.android.feedback.model.payloads.ExtendedData
import apptentive.com.android.feedback.payload.ConversationPayloadService
import apptentive.com.android.feedback.payload.PayloadData
import apptentive.com.android.feedback.payload.PayloadSender
import apptentive.com.android.feedback.payload.PersistentPayloadQueue
import apptentive.com.android.feedback.payload.SerialPayloadSender
import apptentive.com.android.feedback.platform.DefaultAppReleaseFactory
import apptentive.com.android.feedback.platform.DefaultDeviceFactory
import apptentive.com.android.feedback.platform.DefaultEngagementDataFactory
import apptentive.com.android.feedback.platform.DefaultEngagementManifestFactory
import apptentive.com.android.feedback.platform.DefaultPersonFactory
import apptentive.com.android.feedback.platform.DefaultSDKFactory
import apptentive.com.android.feedback.utils.RuntimeUtils
import apptentive.com.android.network.HttpClient
import apptentive.com.android.network.UnexpectedResponseException
import apptentive.com.android.util.FileUtil
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogLevel
import apptentive.com.android.util.Result
import com.apptentive.android.sdk.conversation.DefaultLegacyConversationManager
import com.apptentive.android.sdk.conversation.LegacyConversationManager
import java.io.File

internal class ApptentiveDefaultClient(
    private val apptentiveKey: String,
    private val apptentiveSignature: String,
    private val httpClient: HttpClient,
    private val executors: Executors
) : ApptentiveClient {
    private lateinit var conversationManager: ConversationManager
    private lateinit var payloadSender: PayloadSender
    private lateinit var interactionModules: Map<String, InteractionModule<Interaction>>
    private var engagement: Engagement = NullEngagement()

    //region Initialization

    @WorkerThread
    internal fun start(context: Context, registerCallback: ((result: RegisterResult) -> Unit)?) {
        interactionModules = loadInteractionModules()

        val serialPayloadSender = SerialPayloadSender(
            payloadQueue = PersistentPayloadQueue.create(context),
            callback = ::onPayloadSendFinish
        )
        payloadSender = serialPayloadSender

        val conversationService = createConversationService()
        conversationManager = ConversationManager(
            conversationRepository = createConversationRepository(context),
            conversationService = conversationService,
            legacyConversationManagerProvider = object : Provider<LegacyConversationManager> {
                override fun get() = DefaultLegacyConversationManager(context)
            },
            RuntimeUtils.getApplicationInfo(context).debuggable
        )
        conversationManager.fetchConversationToken {
            when (it) {
                is Result.Error -> {
                    when (val error = it.error) {
                        is UnexpectedResponseException -> {
                            val responseCode = error.statusCode
                            val message = error.errorMessage
                            registerCallback?.invoke(
                                RegisterResult.Failure(
                                    message ?: "Failed to fetch conversation token", responseCode
                                )
                            )
                        }
                        else -> registerCallback?.invoke(RegisterResult.Exception(it.error))
                    }
                }
                is Result.Success -> conversationManager.tryFetchEngagementManifest()
            }
        }
        conversationManager.activeConversation.observe { conversation ->
            if (Log.canLog(LogLevel.Verbose)) { // avoid unnecessary computations
                conversation.logConversation()
            }

            engagement = DefaultEngagement(
                interactionDataProvider = createInteractionDataProvider(conversation),
                interactionConverter = interactionConverter,
                interactionEngagement = createInteractionEngagement(),
                recordEvent = ::recordEvent,
                recordInteraction = ::recordInteraction,
                recordInteractionResponses = ::recordInteractionResponses
            )

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
        }

        // add an observer to track SDK registration
        if (registerCallback != null) {
            var callbackInvoked = false // make sure the initialization callback only gets invoked once
            conversationManager.activeConversation.observe { conversation ->
                val conversationId = conversation.conversationId
                val conversationToken = conversation.conversationToken
                if (conversationId != null && conversationToken != null) {
                    if (!callbackInvoked) {
                        registerCallback.invoke(RegisterResult.Success)
                        callbackInvoked = true
                    }
                }
            }
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
                ApptentiveLifecycleObserver(this, executors.state) {
                    conversationManager.tryFetchEngagementManifest()
                }
            )
        }
    }

    private fun createConversationRepository(context: Context): ConversationRepository {
        return DefaultConversationRepository(
            conversationSerializer = createConversationSerializer(),
            appReleaseFactory = DefaultAppReleaseFactory(context),
            personFactory = DefaultPersonFactory(),
            deviceFactory = DefaultDeviceFactory(context),
            sdkFactory = DefaultSDKFactory(
                version = Constants.SDK_VERSION,
                distribution = "Default",
                distributionVersion = Constants.SDK_VERSION
            ),
            manifestFactory = DefaultEngagementManifestFactory(),
            engagementDataFactory = DefaultEngagementDataFactory()
        )
    }

    private fun createConversationSerializer(): ConversationSerializer {
        return DefaultConversationSerializer(
            conversationFile = getConversationFile(),
            manifestFile = getManifestFile()
        )
    }

    private fun createConversationService(): ConversationService = DefaultConversationService(
        httpClient = httpClient,
        apptentiveKey = apptentiveKey,
        apptentiveSignature = apptentiveSignature,
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
                conversation.engagementData
            ),
            usingCustomStoreUrlSkipInAppReviewID = usingCustomStoreUrlSkipInAppReviewID
        )
    }

    //endregion

    //region Engagement

    override fun engage(event: Event): EngagementResult {
        DependencyProvider.register(EngagementContextProvider(engagement, payloadSender, executors))

        return DependencyProvider.of<EngagementContextFactory>().engagementContext().engage(event)
    }

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

    @WorkerThread
    private fun recordInteraction(interaction: Interaction) {
        conversationManager.recordInteraction(interaction.id)
    }

    @WorkerThread
    private fun recordInteractionResponses(interactionResponses: Map<String, Set<InteractionResponse>>) {
        conversationManager.recordInteractionResponses(interactionResponses)
    }

    @WorkerThread
    private fun onPayloadSendFinish(result: Result<PayloadData>) {
    }

    //endregion

    companion object {
        private fun getConversationFile(): File {
            val conversationsDir = getConversationDir()
            return File(conversationsDir, "conversation.bin")
        }

        private fun getManifestFile(): File {
            val conversationsDir = getConversationDir()
            return File(conversationsDir, "manifest.bin")
        }

        private fun getConversationDir(): File {
            return FileUtil.getInternalDir("conversations", createIfNecessary = true)
        }
    }
}
