package apptentive.com.android.feedback.backend

internal interface ConversationService :
    ConversationFetchService,
    EngagementManifestService,
    ConfigurationService,
    LoginSessionService,
    PayloadRequestSender,
    MessageCenterService
