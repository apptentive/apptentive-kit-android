package apptentive.com.android.feedback

import android.content.Context
import androidx.annotation.WorkerThread
import apptentive.com.android.feedback.backend.ConversationService
import apptentive.com.android.feedback.backend.DefaultConversationService
import apptentive.com.android.feedback.conversation.*
import apptentive.com.android.feedback.engagement.*
import apptentive.com.android.feedback.engagement.interactions.*
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

    //region Initialization

    @WorkerThread
    internal fun start(context: Context) {
        interactionModules = loadInteractionModules()
        conversationService = createConversationService()
        conversationManager = createConversationManager(context) // TODO: get rid of Context
    }

    private fun createConversationManager(context: Context): ConversationManager {
        return ConversationManager(
            conversationRepository = createConversationRepository(context),
            conversationService = conversationService
        )
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
            manifestFactory = DefaultEngagementManifestFactory()
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

    //endregion

    //region Engagement

    override fun engage(context: Context, event: Event): EngagementResult {
        // engagement depends on the Context so should be created each time with a new context object
        val engagement = DefaultEventEngagement(
            interactionResolver = FakeInteractionResolver,
            interactionFactory = interactionFactory,
            interactionEngagement = createInteractionEngagement(context),
            recordEvent = ::recordEvent,
            recordInteraction = ::recordInteraction
        )
        return engagement.engage(event)
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
                module.provideInteractionConverter()
            }
        )
    }

    // FIXME: temporary code
    private fun createInteractionEngagement(context: Context): InteractionEngagement {
        return DefaultInteractionEngagement(
            lookup = interactionLaunchersLookup,
            launchInteraction = { launcher, interaction ->
                launcher.launchInteraction(context, interaction)
            }
        )
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

// FIXME: temporary code
private object FakeInteractionResolver : InteractionResolver {
    override fun getInteraction(event: Event): InteractionData? {
        return if (event.name == "enjoyment_dialog") {
            InteractionData(
                id = "id",
                type = "EnjoymentDialog",
                configuration = mapOf(
                    "title" to "Do you love New SDK?",
                    "yes_text" to "Yes",
                    "no_text" to "No"
                )
            )
        } else {
            null
        }
    }
}