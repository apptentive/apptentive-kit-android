package apptentive.com.android.feedback.backend

internal interface ConversationService :
    ConversationFetchService,
    EngagementManifestService,
    ConfigurationStatusService,
    LoginSessionService,
    PayloadRequestSender,
    MessageCenterService
