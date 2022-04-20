package apptentive.com.android.feedback.backend

import apptentive.com.android.feedback.payload.PayloadRequestSender

internal interface ConversationService :
    ConversationFetchService,
    EngagementManifestService,
    ConfigurationService,
    PayloadRequestSender
