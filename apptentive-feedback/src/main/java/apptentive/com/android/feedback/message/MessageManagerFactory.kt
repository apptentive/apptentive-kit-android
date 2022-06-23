package apptentive.com.android.feedback.message

import apptentive.com.android.core.Provider
import apptentive.com.android.util.InternalUseOnly

@InternalUseOnly
interface MessageManagerFactory {
    fun messageManager(): MessageManager
}

@InternalUseOnly
class MessageManagerFactoryProvider(val messageManager: MessageManager) : Provider<MessageManagerFactory> {
    override fun get(): MessageManagerFactory {
        return object : MessageManagerFactory {
            override fun messageManager(): MessageManager {
                return messageManager
            }
        }
    }
}
