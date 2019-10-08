package apptentive.com.android.feedback

import android.content.Context
import androidx.annotation.WorkerThread
import apptentive.com.android.feedback.backend.ConversationService
import apptentive.com.android.feedback.backend.DefaultConversationService
import apptentive.com.android.feedback.conversation.ConversationManager
import apptentive.com.android.feedback.conversation.DefaultConversationRepository
import apptentive.com.android.feedback.conversation.DefaultConversationSerializer
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

    //region Initialization

    @WorkerThread
    internal fun start(context: Context) {
        conversationService = DefaultConversationService(
            httpClient = httpClient,
            apptentiveKey = apptentiveKey,
            apptentiveSignature = apptentiveSignature,
            apiVersion = Constants.API_VERSION,
            sdkVersion = Constants.SDK_VERSION,
            baseURL = Constants.SERVER_URL
        )
        val conversationFile = getConversationFile(context) // FIXME: remove android specific api
        val manifestFile = getManifestFile(context)
        conversationManager = ConversationManager(
            conversationRepository = DefaultConversationRepository(
                conversationSerializer = DefaultConversationSerializer(
                    conversationFile = conversationFile,
                    manifestFile = manifestFile
                ),
                appReleaseFactory = DefaultAppReleaseFactory(context),
                personFactory = DefaultPersonFactory(),
                deviceFactory = DefaultDeviceFactory(context),
                sdkFactory = DefaultSDKFactory(
                    version = Constants.SDK_VERSION,
                    distribution = "Default",
                    distributionVersion = Constants.SDK_VERSION
                ),
                manifestFactory = DefaultEngagementManifestFactory()
            ),
            conversationService = conversationService
        )
    }

    //endregion

    //region Engagement

    override fun engage(context: Context, event: Event): EngagementResult {
        // engagement depends on the Context so should be created each time with a new context object
        val engagement = DefaultEventEngagement(
            interactionResolver = FakeInteractionResolver,
            interactionFactory = fakeInteractionFactory,
            interactionEngagement = createInteractionEngagement(context),
            recordEvent = ::recordEvent,
            recordInteraction = ::recordInteraction
        )
        return engagement.engage(event)
    }

    // FIXME: temporary code
    private val enjoymentDialog: InteractionModule<Interaction> by lazy {
        val providerClass =
            Class.forName("apptentive.com.android.feedback.ui.EnjoymentDialogModule")
        providerClass.newInstance() as InteractionModule<Interaction>
    }

    // FIXME: temporary code
    private val fakeInteractionFactory: InteractionFactory by lazy {
        DefaultInteractionFactory(
            lookup = mapOf(
                "EnjoymentDialog" to enjoymentDialog.provideInteractionConverter()
            )
        )
    }

    private fun createInteractionEngagement(context: Context): InteractionEngagement {
        return DefaultInteractionEngagement(
            lookup = mapOf(enjoymentDialog.interactionClass to enjoymentDialog.provideInteractionLauncher()),
            launchInteraction = { launcher, interaction ->
                launcher.launchInteraction(context, interaction)
            }
        )
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
        fun getConversationFile(context: Context): File {
            val conversationsDir = getConversationDir(context)
            return File(conversationsDir, "conversation.bin")
        }

        fun getManifestFile(context: Context): File {
            val conversationsDir = getConversationDir(context)
            return File(conversationsDir, "manifest.bin")
        }

        private fun getConversationDir(context: Context): File {
            return FileUtil.getInternalDir(
                context = context,
                path = "conversations",
                createIfNecessary = true
            )
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