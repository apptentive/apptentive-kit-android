package apptentive.com.android.feedback.engagement

import apptentive.com.android.concurrent.Executors
import apptentive.com.android.core.Provider
import apptentive.com.android.feedback.payload.PayloadSender

interface EngagementContextFactory {
    fun engagementContext(): EngagementContext
}

class EngagementContextFactoryProvider(
    private val engagement: Engagement,
    private val payloadSender: PayloadSender,
    private val executor: Executors
) : Provider<EngagementContextFactory> {

    override fun get(): EngagementContextFactory = DefaultEngagementContextFactory(engagement, payloadSender, executor)
}

private class DefaultEngagementContextFactory(
    private val engagement: Engagement,
    private val payloadSender: PayloadSender,
    private val executor: Executors
) : EngagementContextFactory {
    override fun engagementContext(): EngagementContext {
        return EngagementContext(engagement, payloadSender, executor)
    }
}
