package apptentive.com.android.feedback.platform

import android.content.Context
import apptentive.com.android.core.AndroidApplicationInfo
import apptentive.com.android.core.AndroidExecutorFactoryProvider
import apptentive.com.android.core.AndroidLoggerProvider
import apptentive.com.android.core.ApplicationInfo
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.core.ExecutorFactory
import apptentive.com.android.core.MissingProviderException
import apptentive.com.android.core.Provider
import apptentive.com.android.feedback.Apptentive
import apptentive.com.android.feedback.ApptentiveConfiguration
import apptentive.com.android.feedback.conversation.ConversationCredentialProvider
import apptentive.com.android.feedback.conversation.ConversationRepository
import apptentive.com.android.feedback.engagement.EngagementContext
import apptentive.com.android.feedback.engagement.EngagementContextFactory
import apptentive.com.android.feedback.message.MessageManager
import apptentive.com.android.feedback.message.MessageManagerFactory
import apptentive.com.android.feedback.message.MessageRepository
import apptentive.com.android.platform.AndroidSharedPrefDataStore
import apptentive.com.android.platform.DefaultAndroidSharedPrefDataStore
import apptentive.com.android.util.InternalUseOnly
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.FEEDBACK

@InternalUseOnly object ApptentiveKitSDKState {
    private lateinit var applicationContext: Context
    private lateinit var configuration: ApptentiveConfiguration

    fun initialize(applicationContext: Context, configuration: ApptentiveConfiguration) {
        ApptentiveKitSDKState.applicationContext = applicationContext
        ApptentiveKitSDKState.configuration = configuration
        // register dependency providers
        DependencyProvider.register(AndroidLoggerProvider("Apptentive"))
        DependencyProvider.register<ApplicationInfo>(AndroidApplicationInfo(applicationContext))
        DependencyProvider.register<ExecutorFactory>(AndroidExecutorFactoryProvider())
        DependencyProvider.register<FileSystem>(
            AndroidFileSystemProvider(
                applicationContext,
                "apptentive.com.android.feedback"
            )
        )
        DependencyProvider.register<AndroidSharedPrefDataStore>(
            DefaultAndroidSharedPrefDataStore(applicationContext)
        )
    }

    fun getApplicationInfo(): ApplicationInfo {
        try {
            return DependencyProvider.of<ApplicationInfo>()
        } catch (e: Exception) {
            Log.e(FEEDBACK, "Failed to get application info from DependencyProvider. Rebooting SDK")
            rebootSDK()
            return DependencyProvider.of()
        }
    }

    fun getExecutorFactoryProvider(): AndroidExecutorFactoryProvider {
        try {
            return DependencyProvider.of<AndroidExecutorFactoryProvider>()
        } catch (e: Exception) {
            Log.e(FEEDBACK, "Failed to get executor factory provider from DependencyProvider. Rebooting SDK")
            rebootSDK()
            return DependencyProvider.of()
        }
    }

    internal fun getFileSystemProvider(): FileSystem {
        try {
            return DependencyProvider.of<AndroidFileSystemProvider>().get()
        } catch (e: Exception) {
            Log.e(FEEDBACK, "Failed to get file system provider from DependencyProvider. Rebooting SDK")
            rebootSDK()
            return DependencyProvider.of()
        }
    }

    fun getSharedPrefDataStore(): AndroidSharedPrefDataStore {
        try {
            return DependencyProvider.of<AndroidSharedPrefDataStore>()
        } catch (e: Exception) {
            Log.e(FEEDBACK, "Failed to get shared pref data store from DependencyProvider. Rebooting SDK")
            rebootSDK()
            return DependencyProvider.of()
        }
    }

    internal fun addConversationRepository(conversationRepository: ConversationRepository) {
        DependencyProvider.register(conversationRepository)
    }

    internal fun getConversationRepository(): ConversationRepository {
        try {
            return DependencyProvider.of<ConversationRepository>()
        } catch (e: Exception) {
            Apptentive.rebootSDKSubject.value = true
            throw MissingProviderException("Provider is not registered, SDK is rebooted: ${ConversationRepository::class.java}")
        }
    }

    fun addMessageRepository(messageRepository: MessageRepository) {
        DependencyProvider.register(messageRepository)
    }

    fun getMessageRepository(): MessageRepository {
        try {
            return DependencyProvider.of<MessageRepository>()
        } catch (e: Exception) {
            Apptentive.rebootSDKSubject.value = true
            throw MissingProviderException("Provider is not registered, SDK is rebooted: ${MessageRepository::class.java}")
        }
    }

    fun addMessageManager(messageManager: Provider<MessageManagerFactory>) {
        DependencyProvider.register(messageManager)
    }

    fun getMessageManager(): MessageManager {
        try {
            return DependencyProvider.of<MessageManagerFactory>().messageManager()
        } catch (e: Exception) {
            Apptentive.rebootSDKSubject.value = true
            throw MissingProviderException("Provider is not registered, SDK is rebooted: ${MessageManager::class.java}")
        }
    }

    @InternalUseOnly
    fun addEngagementContextFactory(engagementContextProvider: Provider<EngagementContextFactory>) {
        DependencyProvider.register(engagementContextProvider)
    }

    fun getEngagementContext(): EngagementContext {
        try {
            return DependencyProvider.of<EngagementContextFactory>().engagementContext()
        } catch (e: Exception) {
            Apptentive.rebootSDKSubject.value = true
            throw MissingProviderException("Provider is not registered, SDK is rebooted: ${EngagementContext::class.java}")
        }
    }

    fun addConversationCredentialProvider(conversationCredentialProvider: ConversationCredentialProvider) {
        DependencyProvider.register(conversationCredentialProvider)
    }

    fun getConversationCredentialProvider(): ConversationCredentialProvider {
        try {
            return DependencyProvider.of<ConversationCredentialProvider>()
        } catch (e: Exception) {
            Apptentive.rebootSDKSubject.value = true
            throw MissingProviderException("Provider is not registered, SDK is rebooted: ${ConversationCredentialProvider::class.java}")
        }
    }

    private fun rebootSDK() {
        if (this::applicationContext.isInitialized && this::configuration.isInitialized) {
            initialize(applicationContext, configuration)
        } else {
            Log.e(FEEDBACK, "Cannot recover SDK providers. Please reboot the SDK")
            Apptentive.rebootSDKSubject.value = true
        }
    }
}
