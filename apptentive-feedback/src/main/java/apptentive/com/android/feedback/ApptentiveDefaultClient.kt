package apptentive.com.android.feedback

import android.content.Context
import androidx.annotation.WorkerThread
import apptentive.com.android.concurrent.ExecutorQueue
import apptentive.com.android.feedback.backend.BackendService
import apptentive.com.android.feedback.backend.ConversationService
import apptentive.com.android.feedback.conversation.ConversationManager
import apptentive.com.android.network.DefaultHttpClient
import apptentive.com.android.network.DefaultHttpNetwork
import apptentive.com.android.network.DefaultHttpRequestRetryPolicy
import apptentive.com.android.network.HttpNetwork

internal class ApptentiveDefaultClient(
    private val apptentiveKey: String,
    private val apptentiveSignature: String,
    private val network: HttpNetwork,
    private val stateQueue: ExecutorQueue
) : ApptentiveClient {
    private lateinit var conversationService: ConversationService
    private lateinit var conversationManager: ConversationManager

    @WorkerThread
    internal fun start() {
        val httpClient = DefaultHttpClient(
            network = network,
            networkQueue = ExecutorQueue.createConcurrentQueue("Network"),
            retryPolicy = DefaultHttpRequestRetryPolicy()
        )
        conversationService = BackendService(
            httpClient = httpClient,
            apptentiveKey = apptentiveKey,
            apptentiveSignature = apptentiveSignature,
            apiVersion = Constants.API_VERSION,
            sdkVersion = Constants.SDK_VERSION,
            baseURL = Constants.SERVER_URL,
            callbackExecutor = stateQueue
        )
    }

    override fun engage(context: Context, event: String) {
        TODO("Implement me")
    }
}