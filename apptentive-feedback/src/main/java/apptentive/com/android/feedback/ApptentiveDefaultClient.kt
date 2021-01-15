package apptentive.com.android.feedback

import android.content.Context
import androidx.annotation.WorkerThread
import apptentive.com.android.concurrent.Executors
import apptentive.com.android.feedback.backend.ConversationService
import apptentive.com.android.feedback.backend.DefaultConversationService
import apptentive.com.android.feedback.conversation.*
import apptentive.com.android.feedback.engagement.*
import apptentive.com.android.feedback.engagement.criteria.CachedInvocationProvider
import apptentive.com.android.feedback.engagement.criteria.CriteriaInteractionDataProvider
import apptentive.com.android.feedback.engagement.criteria.DefaultTargetingState
import apptentive.com.android.feedback.engagement.criteria.InvocationConverter
import apptentive.com.android.feedback.engagement.interactions.*
import apptentive.com.android.feedback.model.Conversation
import apptentive.com.android.feedback.model.payloads.EventPayload
import apptentive.com.android.feedback.model.payloads.ExtendedData
import apptentive.com.android.feedback.payload.*
import apptentive.com.android.feedback.platform.*
import apptentive.com.android.network.HttpClient
import apptentive.com.android.util.FileUtil
import apptentive.com.android.util.Result
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
    internal fun start(context: Context) {
        interactionModules = loadInteractionModules()

        val serialPayloadSender = SerialPayloadSender(
            payloadQueue = PersistentPayloadQueue.create(context),
            callback = ::onPayloadSendFinish
        )
        payloadSender = serialPayloadSender

        val conversationService = createConversationService()
        conversationManager = ConversationManager(
            conversationRepository = createConversationRepository(context),
            conversationService = conversationService
        )
        conversationManager.activeConversation.observe { conversation ->
            // FIXME: most of these values can be cached and only changed when the actual data changes
            engagement = DefaultEngagement(
                interactionDataProvider = createInteractionDataProvider(conversation),
                interactionConverter = interactionConverter,
                interactionEngagement = createInteractionEngagement(),
                recordEvent = ::recordEvent,
                recordInteraction = ::recordInteraction
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

        // FIXME: temporary code
        engage(context, Event.internal("launch"))
    }

    private fun createConversationRepository(context: Context): ConversationRepository {
        // TODO: refactor this - replace with use cases
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
        return CriteriaInteractionDataProvider(
            interactions = conversation.engagementManifest.interactions.map { it.id to it }.toMap(),
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
            )
        )
    }

    //endregion

    //region Engagement

    override fun engage(context: Context, event: Event): EngagementResult {
        return AndroidEngagementContext(context, engagement, payloadSender, executors).engage(event)
    }

    // FIXME: temporary code
    private val interactionLaunchersLookup: Map<Class<Interaction>, InteractionLauncher<Interaction>> by lazy {
        interactionModules.map { (_, module) ->
            Pair(module.interactionClass, module.provideInteractionLauncher())
        }.toMap()
    }

    // FIXME: temporary code
    private val interactionConverter: InteractionDataConverter by lazy {
        DefaultInteractionDataConverter(
            lookup = interactionModules.mapValues { (_, module) ->
                module.provideInteractionTypeConverter()
            }
        )
    }

    // FIXME: temporary code
    private fun createInteractionEngagement(): InteractionEngagement {
        return DefaultInteractionEngagement(lookup = interactionLaunchersLookup)
    }

    // FIXME: temporary code
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
    private fun onPayloadSendFinish(result: Result<PayloadData>) {
        // TODO: notify the rest of the sdk
    }

    //endregion

    //region Debug

    internal fun reset() {
        conversationManager.clear()
        conversationManager.recordEvent(Event.internal("launch")) // trick sdk to think it was launched
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