package apptentive.com.android.feedback.message

import apptentive.com.android.core.Provider

internal interface MessageManagerFactory {
    fun messageManager(): MessageManager
}

internal class MessageManagerFactoryProvider(val messageManager: MessageManager) : Provider<MessageManagerFactory> {
    override fun get(): MessageManagerFactory {
        return object : MessageManagerFactory {
            override fun messageManager(): MessageManager {
                return messageManager
            }
        }
    }
}
