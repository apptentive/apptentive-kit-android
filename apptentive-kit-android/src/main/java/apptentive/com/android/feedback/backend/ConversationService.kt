package apptentive.com.android.feedback.backend

internal interface ConversationService :
    ConversationFetchService,
    EngagementManifestService,
    StatusService,
    LoginSessionService,
    PayloadRequestSender,
    MessageCenterService
