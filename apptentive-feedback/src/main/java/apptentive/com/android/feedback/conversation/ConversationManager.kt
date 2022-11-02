package apptentive.com.android.feedback.conversation

import androidx.annotation.WorkerThread
import apptentive.com.android.core.BehaviorSubject
import apptentive.com.android.core.Observable
import apptentive.com.android.core.Provider
import apptentive.com.android.core.isInThePast
import apptentive.com.android.feedback.Constants
import apptentive.com.android.feedback.backend.ConversationService
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.criteria.DateTime
import apptentive.com.android.feedback.engagement.interactions.InteractionResponse
import apptentive.com.android.feedback.model.AppRelease
import apptentive.com.android.feedback.model.Conversation
import apptentive.com.android.feedback.model.Device
import apptentive.com.android.feedback.model.EngagementData
import apptentive.com.android.feedback.model.Person
import apptentive.com.android.feedback.model.SDK
import apptentive.com.android.feedback.model.VersionHistory
import apptentive.com.android.feedback.model.hasConversationToken
import apptentive.com.android.feedback.platform.AndroidUtils.currentTimeSeconds
import apptentive.com.android.feedback.utils.VersionCode
import apptentive.com.android.feedback.utils.VersionName
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.CONFIGURATION
import apptentive.com.android.util.LogTags.CONVERSATION
import apptentive.com.android.util.LogTags.ENGAGEMENT_MANIFEST
import apptentive.com.android.util.LogTags.EVENT
import apptentive.com.android.util.LogTags.INTERACTIONS
import apptentive.com.android.util.Result
import com.apptentive.android.sdk.conversation.LegacyConversationManager
import com.apptentive.android.sdk.conversation.toConversation

internal class ConversationManager(
    private val conversationRepository: ConversationRepository,
    private val conversationService: ConversationService,
    private val legacyConversationManagerProvider: Provider<LegacyConversationManager>,
    private val isDebuggable: Boolean
) {
    private val activeConversationSubject: BehaviorSubject<Conversation>
    val activeConversation: Observable<Conversation> get() = activeConversationSubject
    private val sdkAppReleaseUpdateSubject = BehaviorSubject(false)
    val sdkAppReleaseUpdate: Observable<Boolean> get() = sdkAppReleaseUpdateSubject

    var isSDKAppReleaseCheckDone = false

    init {
        val conversation = loadActiveConversation()
        activeConversationSubject = BehaviorSubject(conversation)
        activeConversationSubject.observe(::saveConversation)
        activeConversation.observe(::checkForSDKAppReleaseUpdates)
    }

    fun fetchConversationToken(callback: (result: Result<Unit>) -> Unit) {
        val conversation = activeConversation.value

        // if we have a conversation token - we're good
        if (conversation.hasConversationToken) {
            Log.v(CONVERSATION, "Conversation token already exists")
            callback(Result.Success(Unit))
            return
        }

        Log.v(CONVERSATION, "Fetching conversation token...")
        conversationService.fetchConversationToken(
            device = conversation.device,
            sdk = conversation.sdk,
            appRelease = conversation.appRelease,
            person = conversation.person
        ) {
            when (it) {
                is Result.Error -> {
                    Log.e(CONVERSATION, "Unable to fetch conversation token: ${it.error}")
                    callback(it)
                }
                is Result.Success -> {
                    Log.v(CONVERSATION, "Conversation token fetched successfully")
                    // update current conversation
                    val currentConversation = activeConversationSubject.value
                    activeConversationSubject.value = currentConversation.copy(
                        conversationToken = it.data.token,
                        conversationId = it.data.id,
                        person = currentConversation.person.copy(
                            id = it.data.personId
                        )
                    )

                    // let the caller know fetching was successful
                    callback(Result.Success(Unit))
                }
            }
        }
    }

    fun getConversation() = activeConversation.value

    @Throws(ConversationSerializationException::class)
    @WorkerThread
    private fun loadActiveConversation(): Conversation {
        // load existing conversation
        val existingConversation = conversationRepository.loadConversation()
        if (existingConversation != null) {
            Log.i(CONVERSATION, "Loaded an existing conversation")
            return existingConversation
        }

        // attempt to migrate a legacy conversation
        val legacyConversation = tryMigrateLegacyConversation()
        if (legacyConversation != null) {
            Log.i(CONVERSATION, "Migrated 'legacy' conversation")
            return legacyConversation
        }

        // no active conversations: create a new one
        Log.i(CONVERSATION, "Creating 'anonymous' conversation...")
        return conversationRepository.createConversation()
    }

    fun checkForSDKAppReleaseUpdates(conversation: Conversation) {
        // Check for SDK & AppRelease update once per session
        if (isSDKAppReleaseCheckDone) return else isSDKAppReleaseCheckDone = true
        Log.i(CONVERSATION, "Checking for SDK & AppRelease updates")
        var appReleaseChanged = false
        var sdkChanged = false

        val lastVersionItemSeen = conversation.engagementData.versionHistory.getLastVersionSeen()
        val lastVersionCode: VersionCode? = lastVersionItemSeen?.versionCode
        val lastVersionName: VersionName? = lastVersionItemSeen?.versionName
        val lastSeenSdkVersion: String = conversation.sdk.version

        val currentAppRelease = conversationRepository.getCurrentAppRelease()
        val currentSDK: SDK = conversationRepository.getCurrentSdk()
        val currentVersionCode: VersionCode = currentAppRelease.versionCode
        val currentVersionName: VersionName = currentAppRelease.versionName
        val currentSdkVersion: String = Constants.SDK_VERSION

        if (lastVersionItemSeen == null ||
            currentVersionCode != lastVersionCode ||
            currentVersionName != lastVersionName
        ) {
            Log.d(
                CONVERSATION,
                "Application version was changed: Name: $lastVersionName => $currentVersionName, " +
                    "Code: $lastVersionCode => $currentVersionCode",
            )
            appReleaseChanged = true
        }

        if (lastSeenSdkVersion != currentSdkVersion) {
            sdkChanged = true
            Log.d(CONVERSATION, "SDK version was changed: $lastSeenSdkVersion => $currentSdkVersion")
        }

        if (appReleaseChanged || sdkChanged) {
            sdkAppReleaseUpdateSubject.value = true
            val versionHistory = conversation.engagementData.versionHistory.updateVersionHistory(
                currentTimeSeconds(),
                currentVersionCode,
                currentVersionName
            )
            updateAppReleaseSDK(currentSDK, currentAppRelease, versionHistory)
        }
    }

    @WorkerThread
    private fun saveConversation(conversation: Conversation) {
        try {
            conversationRepository.saveConversation(conversation)
        } catch (exception: Exception) {
            Log.e(CONVERSATION, "Exception while saving conversation")
        }
    }

    @WorkerThread
    fun tryFetchEngagementManifest() {
        val conversation = activeConversationSubject.value
        val manifest = conversation.engagementManifest

        if (isInThePast(manifest.expiry) || isDebuggable) {
            Log.d(CONVERSATION, "Fetching engagement manifest")
            val token = conversation.conversationToken
            val id = conversation.conversationId
            if (token != null && id != null) {
                conversationService.fetchEngagementManifest(
                    conversationToken = token,
                    conversationId = id
                ) {
                    when (it) {
                        is Result.Success -> {
                            Log.d(CONVERSATION, "Engagement manifest successfully fetched")
                            Log.v(ENGAGEMENT_MANIFEST, it.data.toString())
                            activeConversationSubject.value = activeConversationSubject.value.copy(
                                engagementManifest = it.data
                            )
                        }
                        is Result.Error -> {
                            Log.e(
                                CONVERSATION,
                                "Error while fetching engagement manifest",
                                it.error
                            )
                        }
                    }
                }
            } else {
                Log.d(
                    CONVERSATION,
                    "Fetch engagement manifest is not called. " +
                        "Conversation token is $token, conversation id is $id"
                )
            }
        } else {
            Log.d(CONVERSATION, "Engagement manifest up to date")
        }
    }

    @WorkerThread
    fun tryFetchAppConfiguration() {
        val conversation = activeConversationSubject.value
        val configuration = conversation.configuration

        if (isInThePast(configuration.expiry) || isDebuggable) {
            Log.d(CONVERSATION, "Fetching configuration")
            val token = conversation.conversationToken
            val id = conversation.conversationId
            if (token != null && id != null) {
                conversationService.fetchConfiguration(
                    conversationToken = token,
                    conversationId = id
                ) {
                    when (it) {
                        is Result.Success -> {
                            Log.d(CONVERSATION, "Configuration successfully fetched")
                            Log.v(CONFIGURATION, it.data.toString())
                            activeConversationSubject.value = activeConversationSubject.value.copy(
                                configuration = it.data
                            )
                        }
                        is Result.Error -> {
                            Log.e(CONVERSATION, "Error while fetching configuration", it.error)
                        }
                    }
                }
            } else {
                Log.d(
                    CONVERSATION,
                    "Fetch configuration is not called. " +
                        "Conversation token is $token, conversation id is $id"
                )
            }
        } else {
            Log.d(CONVERSATION, "Configuration up to date")
        }
    }

    fun recordEvent(event: Event) {
        Log.v(EVENT, "Recording event: $event")
        val conversation = activeConversationSubject.value
        activeConversationSubject.value = conversation.copy(
            engagementData = conversation.engagementData.addInvoke(
                event = event,
                versionName = conversation.appRelease.versionName,
                versionCode = conversation.appRelease.versionCode,
                lastInvoked = DateTime.now()
            )
        )
    }

    fun updatePerson(person: Person) {
        val conversation = activeConversationSubject.value
        activeConversationSubject.value = conversation.copy(
            person = person
        )
    }

    fun updateDevice(device: Device) {
        val conversation = activeConversationSubject.value
        activeConversationSubject.value = conversation.copy(
            device = device
        )
    }

    fun updateAppReleaseSDK(sdk: SDK, appRelease: AppRelease, versionHistory: VersionHistory) {
        val conversation = activeConversationSubject.value
        val engagementData = activeConversation.value.engagementData
        activeConversationSubject.value = conversation.copy(
            sdk = sdk,
            appRelease = appRelease,
            engagementData = engagementData.copy(versionHistory = versionHistory)
        )
    }

    fun clear() {
        val conversation = activeConversationSubject.value
        activeConversationSubject.value = conversation.copy(
            engagementData = EngagementData()
        )
    }

    fun recordInteraction(interactionId: String) {
        Log.v(INTERACTIONS, "Recording interaction for id: $interactionId")
        val conversation = activeConversationSubject.value
        activeConversationSubject.value = conversation.copy(
            engagementData = conversation.engagementData.addInvoke(
                interactionId = interactionId,
                versionName = conversation.appRelease.versionName,
                versionCode = conversation.appRelease.versionCode,
                lastInvoked = DateTime.now()
            )
        )
    }

    fun recordInteractionResponses(interactionResponses: Map<String, Set<InteractionResponse>>) {
        Log.v(INTERACTIONS, "Recording interaction responses")
        val conversation = activeConversationSubject.value
        activeConversationSubject.value = conversation.copy(
            engagementData = conversation.engagementData.apply {
                interactionResponses.forEach { responses ->
                    addInvoke(
                        interactionId = responses.key,
                        responses = responses.value,
                        versionName = conversation.appRelease.versionName,
                        versionCode = conversation.appRelease.versionCode,
                        lastInvoked = DateTime.now()
                    )
                }
            }
        )
    }

    //region Legacy Conversation

    private fun tryMigrateLegacyConversation(): Conversation? {
        try {
            val legacyConversationManager = legacyConversationManagerProvider.get()
            val legacyConversationData = legacyConversationManager.loadLegacyConversationData()
            if (legacyConversationData != null) {
                return legacyConversationData.toConversation()
            }
        } catch (e: Exception) {
            Log.e(CONVERSATION, "Unable to migrate legacy conversation", e)
        }

        return null
    }

    //endregion
}
