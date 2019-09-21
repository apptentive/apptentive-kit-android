package apptentive.com.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import apptentive.com.android.concurrent.ExecutorQueue
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.feedback.backend.ConversationTokenFetchBody
import apptentive.com.android.feedback.backend.ConversationTokenFetchResponse
import apptentive.com.android.feedback.conversation.ConversationService
import apptentive.com.android.feedback.platform.DefaultAppReleaseFactory
import apptentive.com.android.feedback.platform.DefaultDeviceFactory
import apptentive.com.android.feedback.platform.DefaultSDKFactory
import apptentive.com.android.network.DefaultHttpClient
import apptentive.com.android.network.HttpNetworkImpl
import apptentive.com.android.network.HttpRequestRetryPolicyDefault
import apptentive.com.android.util.Callback

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val SDK_VERSION = "5.4.6"
        val APPTENTIVE_KEY_DEV = "ANDROID-ANDROID-DEV-c9c0b324114f"
        val APPTENTIVE_SIGNATURE_DEV = "98f5539e9310dc290394c68b76664e98"
        val CONFIG_DEFAULT_SERVER_URL = "https://api.apptentive.com"

        val network = HttpNetworkImpl(this)
        val queue = ExecutorQueue.createConcurrentQueue(name = "network");
        val client = DefaultHttpClient(
            network = network,
            networkQueue = queue,
            retryPolicy = HttpRequestRetryPolicyDefault()
        )
        val service = ConversationService(
            httpClient = client,
            apptentiveKey = APPTENTIVE_KEY_DEV,
            apptentiveSignature = APPTENTIVE_SIGNATURE_DEV,
            apiVersion = 9,
            sdkVersion = SDK_VERSION,
            baseURL = CONFIG_DEFAULT_SERVER_URL,
            callbackExecutor = ExecutorQueue.mainQueue
        )

        val device = DefaultDeviceFactory(this).create()
        val sdk = DefaultSDKFactory(
            version = SDK_VERSION,
            distribution = "Default",
            distributionVersion = SDK_VERSION
        ).create()
        val appRelease = DefaultAppReleaseFactory(this).create()
        val payload = ConversationTokenFetchBody.from(
            device = device,
            sdk = sdk,
            appRelease = appRelease
        )
        service.fetchConversationToken(payload, object : Callback<ConversationTokenFetchResponse> {
            override fun onComplete(t: ConversationTokenFetchResponse) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onFailure(t: Throwable) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })
    }
}
