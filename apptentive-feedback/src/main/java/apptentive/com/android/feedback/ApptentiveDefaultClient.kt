package apptentive.com.android.feedback

import android.content.Context
import androidx.annotation.WorkerThread
import apptentive.com.android.concurrent.ExecutorQueue
import apptentive.com.android.feedback.backend.ConversationService
import apptentive.com.android.feedback.backend.DefaultConversationService
import apptentive.com.android.feedback.conversation.ConversationManager
import apptentive.com.android.feedback.conversation.SingleFileConversationSerializer
import apptentive.com.android.feedback.platform.DefaultAppReleaseFactory
import apptentive.com.android.feedback.platform.DefaultDeviceFactory
import apptentive.com.android.feedback.platform.DefaultPersonFactory
import apptentive.com.android.feedback.platform.DefaultSDKFactory
import apptentive.com.android.network.DefaultHttpClient
import apptentive.com.android.network.DefaultHttpRequestRetryPolicy
import apptentive.com.android.network.HttpClient
import apptentive.com.android.network.HttpNetwork
import apptentive.com.android.util.FileUtil
import java.io.File

internal class ApptentiveDefaultClient(
    private val apptentiveKey: String,
    private val apptentiveSignature: String,
    private val httpClient: HttpClient,
    private val stateQueue: ExecutorQueue
) : ApptentiveClient {
    private lateinit var conversationService: ConversationService
    private lateinit var conversationManager: ConversationManager

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
        val conversationSerializer = SingleFileConversationSerializer(conversationFile)
        conversationManager = ConversationManager(
            conversationSerializer = conversationSerializer,
            appReleaseFactory = DefaultAppReleaseFactory(context),
            personFactory = DefaultPersonFactory(),
            deviceFactory = DefaultDeviceFactory(context),
            sdkFactory = DefaultSDKFactory(
                version = Constants.SDK_VERSION,
                distribution = "Default",
                distributionVersion = Constants.SDK_VERSION
            ),
            conversationService = conversationService
        )
    }

    override fun engage(context: Context, event: String) {
        TODO("Implement me")
    }

    companion object {
        fun getConversationFile(context: Context): File {
            val conversationsDir = FileUtil.getInternalDir(
                context = context,
                path = "conversations",
                createIfNecessary = true
            )
            return File(conversationsDir, "single.conversation")
        }
    }
}