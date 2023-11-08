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
import apptentive.com.android.feedback.model.Conversation
import apptentive.com.android.feedback.model.Device
import apptentive.com.android.feedback.model.EngagementData
import apptentive.com.android.feedback.model.EngagementManifest
import apptentive.com.android.feedback.model.Person
import apptentive.com.android.feedback.model.SDK
import apptentive.com.android.feedback.model.VersionHistory
import apptentive.com.android.feedback.platform.AndroidUtils.currentTimeSeconds
import apptentive.com.android.feedback.platform.DefaultStateMachine
import apptentive.com.android.feedback.platform.SDKEvent
import apptentive.com.android.feedback.platform.SDKState
import apptentive.com.android.feedback.utils.FileStorageUtil
import apptentive.com.android.feedback.utils.FileUtil
import apptentive.com.android.feedback.utils.JwtString
import apptentive.com.android.feedback.utils.JwtUtils
import apptentive.com.android.feedback.utils.ThrottleUtils
import apptentive.com.android.feedback.utils.VersionCode
import apptentive.com.android.feedback.utils.VersionName
import apptentive.com.android.feedback.utils.getActiveConversationMetaData
import apptentive.com.android.feedback.utils.getEncryptionKey
import apptentive.com.android.feedback.utils.isMarshmallowOrGreater
import apptentive.com.android.feedback.utils.toSecretKeyBytes
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
import apptentive.com.android.util.isAllNull
import com.apptentive.android.sdk.conversation.LegacyConversationManager
import com.apptentive.android.sdk.conversation.LegacyConversationMetadataItem
import com.apptentive.android.sdk.conversation.toConversation
import com.apptentive.android.sdk.conversation.toConversationRoster
import java.io.File

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

        updateConversationCredentialProvider(conversation.conversationId, conversation.conversationToken, null)
        activeConversationSubject = BehaviorSubject(conversation)
    }

    fun onEncryptionSetupComplete() {
        activeConversationSubject.observe(::saveConversation)
        activeConversation.observe(::checkForSDKAppReleaseUpdates)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun tryFetchConversationToken(callback: (result: Result<Unit>) -> Unit) {
        val conversation = activeConversation.value
        val conversationToken = conversation.conversationToken
        val conversationId = conversation.conversationId
        when {
            // conversation token already exists
            conversationToken != null && conversationId != null -> {
                getActiveConversationMetaData()?.state?.let { activeConversationState ->
                    handleConversationLoadedFromCache(conversationId, conversationToken, activeConversationState)
                    callback(Result.Success(Unit))
                }
            }
            // active conversation token is null, but there is at least one logged out conversation
            isAllNull(conversationId, conversationToken) && DefaultStateMachine.conversationRoster.loggedOut.isNotEmpty() -> {
                DefaultStateMachine.onEvent(SDKEvent.SDKLaunchedAsLoggedOut)
                callback(Result.Success(Unit))
            }
            // no active conversation token, no logged out conversation, fresh start
            else -> fetchNewConversationToken(callback)
        }
    }

    private fun handleConversationLoadedFromCache(conversationId: String, conversationToken: String, state: ConversationState) {
        var encryptionKey: EncryptionKey? = null
        if (state is ConversationState.LoggedIn && isMarshmallowOrGreater()) {
            encryptionKey = state.encryptionWrapperBytes.getEncryptionKey(state.subject)
            DefaultStateMachine.onEvent(SDKEvent.SDKLaunchedAsLoggedIn)
        } else { // Anonymous
            DefaultStateMachine.onEvent(SDKEvent.ConversationAnonymous)
        }
        updateConversationCredentialProvider(conversationId, conversationToken, encryptionKey)
    }

    private fun fetchNewConversationToken(callback: (result: Result<Unit>) -> Unit) {
        Log.v(CONVERSATION, "Fetching new conversation token...")
        DefaultStateMachine.onEvent(SDKEvent.PendingToken)
        val conversation = activeConversation.value
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
                    DefaultStateMachine.onEvent(SDKEvent.ConversationAnonymous)
                    updateConversationCredentialProvider(it.data.id, it.data.token, null)
                    activeConversationSubject.value = conversation.copy(
                        conversationToken = it.data.token,
                        conversationId = it.data.id,
                        person = conversation.person.copy(
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

        // no active conversation: create a new one
        Log.i(CONVERSATION, "Creating 'anonymous' conversation...")
        return createConversation()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    internal fun loginSession(
        conversationId: String,
        jwtToken: String,
        subject: String,
        legacyConversationPath: String? = null,
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
                    val key = result.data.encryptionKey.getKeyFromHexString()
                    val encryptionKey = EncryptionKey(key, KeyResolver23.getTransformation())
                    val encryptedBytes = key.toSecretKeyBytes(subject)

                    DefaultStateMachine.onEvent(SDKEvent.LoggedIn(subject, encryptionKey, encryptedBytes))
                    // Don't re-load conversation that was upgraded from anonymous
                    if (previousState == SDKState.LOGGED_OUT) {
                        try {
                            val conversation = if (legacyConversationPath == null) {
                                loadExistingConversation() ?: createConversation(conversationId, jwtToken)
                            } else {
                                // Conversation cache is still in legacy format, should try to migrate that
                                tryMigrateEncryptedLoggedOutLegacyConversation(
                                    LegacyConversationMetadataItem(
                                        conversationId,
                                        jwtToken,
                                        File(legacyConversationPath),
                                        result.data.encryptionKey,
                                        subject,
                                    )
                                )
                            }
                            activeConversationSubject.value = conversation.copy(
                                conversationId = conversation.conversationId,
                                conversationToken = jwtToken
                            )
                            tryFetchEngagementManifest()
                            tryFetchAppConfiguration()
                        } catch (e: ConversationSerializationException) {
                            Log.e(CONVERSATION, "Failed to load conversation from cache", e)
                            DefaultStateMachine.onEvent(SDKEvent.Error)
                            loginCallback?.invoke(LoginResult.Exception(e))
                        }
                    } else {
                        activeConversationSubject.value = activeConversation.value.copy(
                            conversationToken = jwtToken
                        )
                        FileStorageUtil.deleteMessageFile() // delete previously stored message file as it would be cached with different encryption setting
                    }
                    updateConversationCredentialProvider(conversationId, jwtToken, encryptionKey)
                    loginCallback?.invoke(LoginResult.Success)
                }
            }
        }
    }

    internal fun logoutSession(callback: (Result<Unit>) -> Unit) {
        val conversationId = activeConversation.value.conversationId
        if (conversationId == null) {
            Log.e(CONVERSATION, "Cannot logout session, conversation id is null")
            callback(Result.Error("Cannot logout session, conversation id is null", Exception()))
        } else {
            callback(Result.Success(Unit)) // Callback immediately to send the logout event and payload before encryption is removed.
            DefaultStateMachine.onEvent(SDKEvent.Logout(conversationId))
            activeConversationSubject.value = conversationRepository.createConversation()
            updateConversationCredentialProvider(null, null, null)
            Log.v(CONVERSATION, "Logout session successful, logged out conversation")
        }
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
                    val key = it.data.encryptionKey.getKeyFromHexString()
                    val encryptionKey = EncryptionKey(key, KeyResolver23.getTransformation())
                    val encryptedBytes = key.toSecretKeyBytes(subject)
                    DefaultStateMachine.onEvent(SDKEvent.LoggedIn(subject, encryptionKey, encryptedBytes))
                    updateConversationCredentialProvider(it.data.id, jwtToken, encryptionKey)
                    activeConversationSubject.value = conversation.copy(
                        conversationToken = jwtToken,
                        conversationId = it.data.id,
                        person = conversation.person.copy(
                            id = it.data.personId
                        )
                    )
                    tryFetchEngagementManifest()
                    tryFetchAppConfiguration()
                    loginCallback?.invoke(LoginResult.Success)
                }
            }
        }
    }

    internal fun updateToken(jwtToken: JwtString, callback: ((result: LoginResult) -> Unit)? = null) {
        val activeConversationMetaData = getActiveConversationMetaData()
        val subClaim = JwtUtils.extractSub(jwtToken)

        if (subClaim == null) {
            callback?.invoke(LoginResult.Error("Invalid JWT token"))
            return
        }

        if (activeConversationMetaData?.state is ConversationState.LoggedIn && (activeConversationMetaData.state as ConversationState.LoggedIn).subject == subClaim) {
            Log.d(CONVERSATION, "Refreshing the auth token for the user with subject: $subClaim")
            val conversationCredential = DependencyProvider.of<ConversationCredentialProvider>()
            updateConversationCredentialProvider(conversationCredential.conversationId, jwtToken, conversationCredential.payloadEncryptionKey)
            activeConversationSubject.value = getConversation().copy(
                conversationToken = jwtToken,
            )
            // TODO update the token for enqueued payloads/failed payloads
            callback?.invoke(LoginResult.Success)
        } else {
            Log.d(CONVERSATION, "Cannot refresh the auth token for the user with subject: $subClaim")
            callback?.invoke(LoginResult.Error("Cannot refresh the auth token for the user with subject: $subClaim"))
        }
    }

    private fun updateConversationCredentialProvider(id: String?, token: String?, payloadEncryptionKey: EncryptionKey?) {
        DependencyProvider.register<ConversationCredentialProvider> (
            ConversationCredential(
                conversationId = id,
                conversationToken = token,
                payloadEncryptionKey = payloadEncryptionKey,
                conversationPath = DefaultStateMachine.conversationRoster.activeConversation?.path
            )
        )
    }

    internal fun setManifestExpired() {
        val conversation = activeConversationSubject.value
        activeConversationSubject.value = conversation.copy(
            engagementManifest = conversation.engagementManifest.copy(
                expiry = 0.0 // set to 0 to force a fetch
            )
        )
        Log.v(CONVERSATION, "Engagement manifest is set to expire ${activeConversationSubject.value.engagementManifest.expiry}")
    }

    private fun createConversation(conversationId: String? = null, conversationToken: String? = null): Conversation {
        return conversationRepository.createConversation(conversationId, conversationToken)
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

        val currentAppRelease = conversationRepository.getCurrentAppRelease()
        val currentSDK: SDK = conversationRepository.getCurrentSdk()
        val currentVersionCode: VersionCode = currentAppRelease.versionCode
        val currentVersionName: VersionName = currentAppRelease.versionName

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

        if (conversation.sdk != currentSDK) {
            sdkChanged = true
            Log.d(CONVERSATION, "SDK updated: ${conversation.sdk.version} (${conversation.sdk.distribution} ${conversation.sdk.distributionVersion}) => ${currentSDK.version} (${currentSDK.distribution} ${currentSDK.distributionVersion})")
            Log.v(CONVERSATION, "SDK full changes: ${conversation.sdk} => $currentSDK")
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
        } catch (e: ConversationLoggedOutException) {
            Log.w(CONVERSATION, "No active conversation found in the roster, cannot save conversation")
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
            val legacyConversationMetaData = legacyConversationManager.loadLegacyConversationMetadata()
            if (legacyConversationMetaData != null && legacyConversationMetaData.hasItems()) {
                val roster = legacyConversationMetaData.toConversationRoster()
                DefaultStateMachine.onEvent(SDKEvent.FoundLegacyConversation(roster))
                val legacyConversationData = legacyConversationManager.loadLegacyConversationData(legacyConversationMetaData)
                if (legacyConversationData != null) {
                    return legacyConversationData.toConversation()
                } else {
                    Log.e(CONVERSATION, "Unable to migrate legacy conversation")
                }
            }
        } catch (e: Exception) {
            Log.e(CONVERSATION, "Unable to migrate legacy conversation", e)
        }

        return null
    }

    private fun tryMigrateEncryptedLoggedOutLegacyConversation(conversationMetaDataItem: LegacyConversationMetadataItem): Conversation {
        try {
            val legacyConversationManager = legacyConversationManagerProvider.get()
            val legacyConversationData = legacyConversationManager.loadEncryptedLegacyConversationData(conversationMetaDataItem)
            if (legacyConversationData != null) {
                return legacyConversationData.toConversation()
            } else {
                Log.e(CONVERSATION, "Unable to login legacy conversation, creating a new conversation")
            }
        } catch (e: Exception) {
            Log.e(CONVERSATION, "Unable to login legacy conversation, creating a new conversation", e)
        }
        return createConversation(conversationMetaDataItem.conversationId, conversationMetaDataItem.conversationToken)
    }

    //endregion
}
