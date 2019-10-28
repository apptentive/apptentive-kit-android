package apptentive.com.android.feedback

import android.content.Context
import androidx.annotation.WorkerThread
import apptentive.com.android.feedback.backend.ConversationService
import apptentive.com.android.feedback.backend.DefaultConversationService
import apptentive.com.android.feedback.conversation.*
import apptentive.com.android.feedback.engagement.*
import apptentive.com.android.feedback.engagement.criteria.CachedInvocationRepository
import apptentive.com.android.feedback.engagement.criteria.InvocationConverter
import apptentive.com.android.feedback.engagement.criteria.DefaultTargetingState
import apptentive.com.android.feedback.engagement.interactions.*
import apptentive.com.android.feedback.model.Conversation
import apptentive.com.android.feedback.platform.*
import apptentive.com.android.network.HttpClient
import apptentive.com.android.util.FileUtil
import java.io.File

internal class ApptentiveDefaultClient(
    private val apptentiveKey: String,
    private val apptentiveSignature: String,
    private val httpClient: HttpClient
) : ApptentiveClient {
    private lateinit var conversationService: ConversationService
    private lateinit var conversationManager: ConversationManager
    private lateinit var interactionModules: Map<InteractionType, InteractionModule<Interaction>>
    private var engagement: EventEngagement = NullEventEngagement()

    //region Initialization

    @WorkerThread
    internal fun start(context: Context) {
        interactionModules = loadInteractionModules()
        conversationService = createConversationService()
        conversationManager = createConversationManager(context) // TODO: get rid of Context
        conversationManager.activeConversation.observe { conversation ->
            // FIXME: most of these values can be cached and only changed when the actual data changes
            engagement = DefaultEventEngagement(
                interactions = createInteractionRepository(conversation),
                interactionFactory = interactionFactory,
                interactionEngagement = createInteractionEngagement(),
                recordEvent = ::recordEvent,
                recordInteraction = ::recordInteraction
            )
        }

        // FIXME: temporary code
        engage(context, Event.internal("launch"))
    }

    private fun createConversationManager(context: Context): ConversationManager {
        return ConversationManager(
            conversationRepository = createConversationRepository(context),
            conversationService = conversationService
        )
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

    private fun createInteractionRepository(conversation: Conversation): InteractionRepository {
        return CriteriaInteractionRepository(
            interactions = conversation.engagementManifest.interactions.map { it.id to it }.toMap(),
            invocations = CachedInvocationRepository(
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
        val engagementContext = AndroidEngagementContext(context, engagement)
        return engagement.engage(engagementContext, event)
    }

    // FIXME: temporary code
    private val interactionLaunchersLookup: Map<Class<Interaction>, InteractionLauncher<Interaction>> by lazy {
        interactionModules.map { (_, module) ->
            Pair(module.interactionClass, module.provideInteractionLauncher())
        }.toMap()
    }

    // FIXME: temporary code
    private val interactionFactory: InteractionFactory by lazy {
        DefaultInteractionFactory(
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
        val component = InteractionModuleComponent(
            interactionNames = interactionNames,
            packageName = "apptentive.com.android.feedback.ui",
            classSuffix = "Module"
        )
        return component.getModules()
    }

    // FIXME: temporary code
    private fun recordEvent(event: Event) {
        conversationManager.recordEvent(event)
    }

    // FIXME: temporary code
    private fun recordInteraction(interaction: Interaction) {
        conversationManager.recordInteraction(interaction.id)
    }

    //endregion

    companion object {
        // FIXME: temporary code
        private val interactionNames = listOf(
            "UpgradeMessage",
            "EnjoymentDialog",
            "RatingDialog",
            "MessageCenter",
            "AppStoreRating",
            "Survey",
            "TextModal",
            "NavigateToLink"
        )

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