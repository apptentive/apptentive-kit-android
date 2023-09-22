package apptentive.com.android.feedback.conversation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.WorkerThread
import apptentive.com.android.core.BehaviorSubject
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.core.Observable
import apptentive.com.android.core.Provider
import apptentive.com.android.core.isInThePast
import apptentive.com.android.encryption.Encryption
import apptentive.com.android.encryption.EncryptionKey
import apptentive.com.android.encryption.KeyResolver23
import apptentive.com.android.encryption.getKeyFromHexString
import apptentive.com.android.feedback.Constants
import apptentive.com.android.feedback.LoginResult
import apptentive.com.android.feedback.backend.ConversationService
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.criteria.DateTime
import apptentive.com.android.feedback.engagement.interactions.InteractionResponse
import apptentive.com.android.feedback.model.AppRelease
import apptentive.com.android.feedback.model.Configuration
import apptentive.com.android.feedback.model.Conversation
import apptentive.com.android.feedback.model.Device
import apptentive.com.android.feedback.model.EngagementData
import apptentive.com.android.feedback.model.EngagementManifest
import apptentive.com.android.feedback.model.Person
import apptentive.com.android.feedback.model.RandomSampling
import apptentive.com.android.feedback.model.SDK
import apptentive.com.android.feedback.model.VersionHistory
import apptentive.com.android.feedback.platform.AndroidUtils.currentTimeSeconds
import apptentive.com.android.feedback.platform.DefaultStateMachine
import apptentive.com.android.feedback.platform.SDKEvent
import apptentive.com.android.feedback.platform.SDKState
import apptentive.com.android.feedback.utils.FileUtil
import apptentive.com.android.feedback.utils.ThrottleUtils
import apptentive.com.android.feedback.utils.VersionCode
import apptentive.com.android.feedback.utils.VersionName
import apptentive.com.android.network.UnexpectedResponseException
import apptentive.com.android.platform.AndroidSharedPrefDataStore
import apptentive.com.android.platform.SharedPrefConstants.SDK_CORE_INFO
import apptentive.com.android.platform.SharedPrefConstants.SDK_VERSION
import apptentive.com.android.serialization.json.JsonConverter
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
    private var isUsingLocalManifest: Boolean = false
    private val activeConversationSubject: BehaviorSubject<Conversation>
    val activeConversation: Observable<Conversation> get() = activeConversationSubject
    private val sdkAppReleaseUpdateSubject = BehaviorSubject(false)
    val sdkAppReleaseUpdate: Observable<Boolean> get() = sdkAppReleaseUpdateSubject

    var isSDKAppReleaseCheckDone = false

    init {
        val conversation = loadActiveConversation()

        // Store successful SDK version after loading conversation
        DependencyProvider.of<AndroidSharedPrefDataStore>()
            .putString(SDK_CORE_INFO, SDK_VERSION, Constants.SDK_VERSION)

        activeConversationSubject = BehaviorSubject(conversation)
    }

    fun onEncryptionSetupComplete() {
        activeConversationSubject.observe(::saveConversation)
        activeConversation.observe(::checkForSDKAppReleaseUpdates)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    internal fun createConversationAndLogin(jwtToken: String, subject: String, loginCallback: ((result: LoginResult) -> Unit)? = null) {
        val conversation = createConversation()
        conversationService.fetchLoginConversation(
            conversation.device,
            conversation.sdk,
            conversation.appRelease,
            conversation.person,
            jwtToken
        ) {
            when (it) {
                is Result.Error -> {
                    when (val error = it.error) {
                        is UnexpectedResponseException -> {
                            val responseCode = error.statusCode
                            val message = error.errorMessage
                            loginCallback?.invoke(LoginResult.Failure(message ?: "Failed to login", responseCode))
                        }
                        else -> loginCallback?.invoke(LoginResult.Exception(it.error))
                    }
                }

                is Result.Success -> {
                    val encryptionKey = EncryptionKey(
                        it.data.encryptionKey.getKeyFromHexString(),
                        KeyResolver23.getTransformation()
                    )
                    DefaultStateMachine.onEvent(SDKEvent.LoggedIn(subject, encryptionKey))
                    updateConversationIdentifierProvider(it.data.id, it.data.token, encryptionKey)
                    activeConversationSubject.value = conversation.copy(
                        conversationToken = it.data.token,
                        conversationId = it.data.id,
                        person = conversation.person.copy(
                            id = it.data.personId
                        )
                    )

                    tryFetchEngagementManifest()
                    tryFetchAppConfiguration()

                    Log.d(CONVERSATION, "Login session successful, " + "conversationId: ${it.data.id}")
                    loginCallback?.invoke(LoginResult.Success)
                }
            }
        }
    }

    /**
     * Starts or retrieves a logged in conversation
     *
     * Start a session from anonymous -> logged in
     * or
     * Resume a session from logged out -> logged in
     *
     * @param conversationId - The conversation ID retrieved from the roster
     * @param jwtToken - The customer provided JWT token
     */
    @RequiresApi(Build.VERSION_CODES.M)
    internal fun loginSession(
        conversationId: String,
        jwtToken: String,
        subject: String,
        loginCallback: ((result: LoginResult) -> Unit)? = null
    ) {
        conversationService.loginSession(conversationId, jwtToken) { result ->
            when (result) {
                is Result.Error -> {
                    when (val error = result.error) {
                        is UnexpectedResponseException -> {
                            val responseCode = error.statusCode
                            val message = error.errorMessage
                            loginCallback?.invoke(LoginResult.Failure(message ?: "Failed to login", responseCode))
                        }
                        else -> loginCallback?.invoke(LoginResult.Exception(result.error))
                    }
                }

                is Result.Success -> {
                    Log.v(CONVERSATION, "Login session successful, encryption key: ${result.data.encryptionKey.getKeyFromHexString()}")
                    val previousState = DefaultStateMachine.state
                    val encryptionKey = EncryptionKey(
                        result.data.encryptionKey.getKeyFromHexString(),
                        KeyResolver23.getTransformation()
                    )
                    DefaultStateMachine.onEvent(SDKEvent.LoggedIn(subject, encryptionKey))
                    if (previousState == SDKState.LOGGED_OUT) {
                        loadExistingConversation()?.let { // TODO handle if null
                            updateConversationIdentifierProvider(it.conversationId, it.conversationToken, encryptionKey)
                            activeConversationSubject.value = it
                            tryFetchEngagementManifest()
                            tryFetchAppConfiguration()
                        } // Don't re-load conversation that was upgraded from anonymous to logged in
                    }
                    loginCallback?.invoke(LoginResult.Success)
                }
            }
        }
    }

    internal fun logoutSession() {
        val conversationId = activeConversation.value.conversationId
        if (conversationId == null) {
            Log.e(CONVERSATION, "Cannot logout session, conversation id is null")
            return
        } else {
            DefaultStateMachine.onEvent(SDKEvent.Logout(conversationId))
            setManifestExpired()
            activeConversationSubject.value = activeConversation.value.copy( // TODO reset SDK and AppRelease
                conversationToken = null,
                conversationId = null,
                person = Person(),
                configuration = Configuration(),
                randomSampling = RandomSampling(),
                engagementData = EngagementData(),
                engagementManifest = EngagementManifest()
            )
            Log.v(CONVERSATION, "Logout session successful, logged out conversation")
        }
    }

    private fun updateConversationIdentifierProvider(id: String?, token: String?, encryptionKey: EncryptionKey?) {
        DependencyProvider.register(ConversationCredentials(
            conversationId = id,
            conversationToken = token,
            payloadEncryptionKey = encryptionKey,
            conversationPath = DefaultStateMachine.conversationRoster.activeConversation?.path
        ))
    }

    private fun setManifestExpired() {
        val conversation = activeConversationSubject.value
        activeConversationSubject.value = conversation.copy(
            engagementManifest = conversation.engagementManifest.copy(
                expiry = 0.0 // set to 0 to force a fetch
            )
        )
    }

    fun fetchConversationToken(callback: (result: Result<Unit>) -> Unit) {
        val conversation = activeConversation.value
        val conversationToken = conversation.conversationToken
        val conversationId = conversation.conversationId
        if (conversationToken != null && conversationId != null) {
            Log.v(CONVERSATION, "Conversation token already exists")
            when (DefaultStateMachine.conversationRoster.activeConversation?.state) {
                is ConversationState.LoggedIn -> {
                    Log.v(CONVERSATION, "Identified as logged in conversation")
                    val loggedIn = DefaultStateMachine.conversationRoster.activeConversation?.state as ConversationState.LoggedIn
                    DefaultStateMachine.onEvent(
                        SDKEvent.LoggedIn(
                            loggedIn.subject,
                            loggedIn.encryptionKey,
                        )
                    )
                }
                is ConversationState.Anonymous -> {
                    Log.v(CONVERSATION, "Identified as anonymous conversation")
                    DefaultStateMachine.onEvent(SDKEvent.ConversationLoaded)
                }
                // TODO should I handle this else case?
                else -> {
                    Log.v(CONVERSATION, "Conversation token already exists, but not logged in")
                    DefaultStateMachine.onEvent(SDKEvent.Error)
                }
            }
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
                    DefaultStateMachine.onEvent(SDKEvent.Error)
                    callback(it)
                }
                is Result.Success -> {
                    Log.v(CONVERSATION, "Conversation token fetched successfully")
                    DefaultStateMachine.onEvent(SDKEvent.ConversationLoaded)
                    // update current conversation
                    val currentConversation = activeConversationSubject.value
                    updateConversationIdentifierProvider(it.data.id, it.data.token, null)
                    activeConversationSubject.value = currentConversation.copy(
                        conversationToken = it.data.token,
                        conversationId = it.data.id,
                        person = currentConversation.person.copy(
                            id = it.data.personId
                        )
                    )
                    callback(Result.Success(Unit))
                }
            }
        }
    }

    fun getConversation() = activeConversation.value

    fun updateEncryption(encryption: Encryption) {
        conversationRepository.updateEncryption(encryption)
    }

    @Throws(ConversationSerializationException::class)
    @WorkerThread
    private fun loadActiveConversation(): Conversation {
        DefaultStateMachine.onEvent(SDKEvent.LoadingConversation)
        // load existing conversation
        val existingConversation = loadExistingConversation()
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
//        Log.i(CONVERSATION, "Creating 'anonymous' conversation...")
        return createConversation()
    }

    internal fun createConversation(): Conversation {
        DefaultStateMachine.onEvent(SDKEvent.PendingToken)
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
            Log.d(CONVERSATION, "Conversation saved successfully")
        } catch (exception: Exception) {
            Log.e(CONVERSATION, "Exception while saving conversation")
        }
    }

    fun setTestManifestFromLocal(json: String) {
        if (isDebuggable) {
            val data: EngagementManifest =
                JsonConverter.fromJson(json, EngagementManifest::class.java) as EngagementManifest
            Log.d(CONVERSATION, "Parsed engagement manifest $data")
            activeConversationSubject.value = activeConversation.value.copy(
                engagementManifest = data
            )
            Log.d(CONVERSATION, "USING LOCALLY DOWNLOADED MANIFEST")
            isUsingLocalManifest = true
        }
    }

    @WorkerThread
    fun tryFetchEngagementManifest() {
        val conversation = activeConversationSubject.value
        val manifest = conversation.engagementManifest

        if (isInThePast(manifest.expiry) || isDebuggable && !isUsingLocalManifest) {
            Log.d(CONVERSATION, "Fetching engagement manifest")
            val token = conversation.conversationToken
            val id = conversation.conversationId
            if (token != null && id != null) {
                fetchEngagementManifest(id, token)
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

    private fun fetchEngagementManifest(conversationId: String, conversationToken: String) {
        conversationService.fetchEngagementManifest(
            conversationToken = conversationToken,
            conversationId = conversationId
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

    fun recordCurrentResponse(interactionResponses: Map<String, Set<InteractionResponse>>, reset: Boolean = false) {
        val conversation = activeConversationSubject.value
        activeConversationSubject.value = conversation.copy(
            engagementData = conversation.engagementData.apply {
                interactionResponses.forEach { responses ->
                    Log.v(INTERACTIONS, "Recording interaction responses ${responses.key to responses.value}")
                    updateCurrentAnswer(
                        interactionId = responses.key,
                        responses = responses.value,
                        versionName = conversation.appRelease.versionName,
                        versionCode = conversation.appRelease.versionCode,
                        lastInvoked = DateTime.now(),
                        reset = reset
                    )
                }
            }
        )
    }

    internal fun loadExistingConversation(): Conversation? {
        return try {
            conversationRepository.loadConversation()
        } catch (e: ConversationSerializationException) {
            // This fix is to recover the accounts that are stuck with serialization issue.
            // It is not recommended to reset the conversation state.

            if (!ThrottleUtils.shouldThrottleResetConversation()) {
                Log.e(CONVERSATION, "Cannot load existing conversation", e)
                Log.d(CONVERSATION, "Deserialization failure, deleting the conversation files")
                FileUtil.deleteUnrecoverableStorageFiles(FileUtil.getInternalDir("conversations"))
                null
            } else {
                throw ConversationSerializationException("Cannot load existing conversation, conversation reset throttled", e)
            }
        }
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
