package apptentive.com.android.feedback.conversation

import apptentive.com.android.feedback.CONVERSATION
import apptentive.com.android.feedback.model.*
import apptentive.com.android.serialization.*
import apptentive.com.android.util.Log
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File

interface ConversationSerializer {
    @Throws(ConversationSerializationException::class)
    fun loadConversation(): Conversation?

    @Throws(ConversationSerializationException::class)
    fun saveConversation(conversation: Conversation)
}

internal class DefaultConversationSerializer(
    private val conversationFile: File,
    private val manifestFile: File
) : ConversationSerializer {
    private var shouldSaveManifest: Boolean = true

    // TODO: unit tests
    override fun saveConversation(conversation: Conversation) {
        conversationFile.outputStream().use { stream ->
            val encoder = BinaryEncoder(DataOutputStream(stream))
            encoder.encodeConversation(conversation)
        }
        if (shouldSaveManifest) {
            manifestFile.outputStream().use { stream ->
                val encoder = BinaryEncoder(DataOutputStream(stream))
                encoder.encodeEngagementManifest(conversation.engagementManifest)
            }
            shouldSaveManifest = false
        }
    }

    override fun loadConversation(): Conversation? {
        if (conversationFile.exists()) {
            val conversation = conversationFile.inputStream().use { stream ->
                val encoder = BinaryDecoder(DataInputStream(stream))
                encoder.decodeConversation()
            }
            val engagementManifest = loadEngagementManifest()
            if (engagementManifest != null) {
                return conversation.copy(engagementManifest = engagementManifest)
            }

            return conversation
        }

        return null
    }

    private fun loadEngagementManifest(): EngagementManifest? {
        if (manifestFile.exists()) {
            try {
                return manifestFile.inputStream().use { stream ->
                    val encoder = BinaryDecoder(DataInputStream(stream))
                    encoder.decodeEngagementManifest()
                }
            } catch (e: Exception) {
                Log.e(CONVERSATION, "Exception while loading manifest file", e)
            }
        }
        return null
    }
}

private fun Encoder.encodeConversation(obj: Conversation) {
    encodeString(obj.localIdentifier)
    encodeNullableString(obj.conversationToken)
    encodeNullableString(obj.conversationId)
    encodeDevice(obj.device)
    encodePerson(obj.person)
    encodeSDK(obj.sdk)
    encodeAppRelease(obj.appRelease)
    encodeEngagementData(obj.engagementData)
}

private fun Encoder.encodeDevice(obj: Device) {
    encodeString(obj.osName)
    encodeString(obj.osVersion)
    encodeString(obj.osBuild)
    encodeInt(obj.osApiLevel)
    encodeString(obj.manufacturer)
    encodeString(obj.model)
    encodeString(obj.board)
    encodeString(obj.product)
    encodeString(obj.brand)
    encodeString(obj.cpu)
    encodeString(obj.device)
    encodeString(obj.uuid)
    encodeString(obj.buildType)
    encodeString(obj.buildId)
    encodeNullableString(obj.carrier)
    encodeNullableString(obj.currentCarrier)
    encodeNullableString(obj.networkType)
    encodeNullableString(obj.bootloaderVersion)
    encodeNullableString(obj.radioVersion)
    encodeString(obj.localeCountryCode)
    encodeString(obj.localeLanguageCode)
    encodeString(obj.localeRaw)
    encodeInt(obj.utcOffset)
    encodeNullableString(obj.advertiserId)
    encodeCustomData(obj.customData)
    encodeIntegrationConfigItem(obj.integrationConfig)
}

private fun Encoder.encodePerson(obj: Person) {
    encodeNullableString(obj.id)
    encodeNullableString(obj.email)
    encodeNullableString(obj.name)
    encodeNullableString(obj.facebookId)
    encodeNullableString(obj.phoneNumber)
    encodeNullableString(obj.street)
    encodeNullableString(obj.city)
    encodeNullableString(obj.zip)
    encodeNullableString(obj.country)
    encodeNullableString(obj.birthday)
    encodeNullableString(obj.mParticleId)
    encodeCustomData(obj.customData)
}

private fun Encoder.encodeSDK(obj: SDK) {
    encodeString(obj.version)
    encodeString(obj.platform)
    encodeString(obj.distribution)
    encodeString(obj.distributionVersion)
    encodeNullableString(obj.programmingLanguage)
    encodeNullableString(obj.authorName)
    encodeNullableString(obj.authorEmail)
}

private fun Encoder.encodeAppRelease(obj: AppRelease) {
    encodeString(obj.type)
    encodeString(obj.identifier)
    encodeLong(obj.versionCode)
    encodeString(obj.versionName)
    encodeString(obj.targetSdkVersion)
    encodeBoolean(obj.debug)
    encodeBoolean(obj.inheritStyle)
    encodeBoolean(obj.overrideStyle)
    encodeNullableString(obj.appStore)
}

private fun Encoder.encodeEngagementData(engagementData: EngagementData) {
    // FIXME: encode engagement data
}

private fun Encoder.encodeEngagementManifest(obj: EngagementManifest) {
    // FIXME: encode manifest
}

private fun Decoder.decodeConversation(): Conversation {
    return Conversation(
        localIdentifier = decodeString(),
        conversationToken = decodeNullableString(),
        conversationId = decodeNullableString(),
        device = decodeDevice(),
        person = decodePerson(),
        sdk = decodeSDK(),
        appRelease = decodeAppRelease(),
        engagementManifest = EngagementManifest(),
        engagementData = decodeEngagementData()
    )
}

private fun Decoder.decodeDevice(): Device {
    return Device(
        osName = decodeString(),
        osVersion = decodeString(),
        osBuild = decodeString(),
        osApiLevel = decodeInt(),
        manufacturer = decodeString(),
        model = decodeString(),
        board = decodeString(),
        product = decodeString(),
        brand = decodeString(),
        cpu = decodeString(),
        device = decodeString(),
        uuid = decodeString(),
        buildType = decodeString(),
        buildId = decodeString(),
        carrier = decodeNullableString(),
        currentCarrier = decodeNullableString(),
        networkType = decodeNullableString(),
        bootloaderVersion = decodeNullableString(),
        radioVersion = decodeNullableString(),
        localeCountryCode = decodeString(),
        localeLanguageCode = decodeString(),
        localeRaw = decodeString(),
        utcOffset = decodeInt(),
        advertiserId = decodeNullableString(),
        customData = decodeCustomData(),
        integrationConfig = decodeIntegrationConfig()
    )
}

private fun Decoder.decodePerson(): Person {
    return Person(
        id = decodeNullableString(),
        email = decodeNullableString(),
        name = decodeNullableString(),
        facebookId = decodeNullableString(),
        phoneNumber = decodeNullableString(),
        street = decodeNullableString(),
        city = decodeNullableString(),
        zip = decodeNullableString(),
        country = decodeNullableString(),
        birthday = decodeNullableString(),
        mParticleId = decodeNullableString(),
        customData = decodeCustomData()
    )
}

private fun Decoder.decodeSDK(): SDK {
    return SDK(
        version = decodeString(),
        platform = decodeString(),
        distribution = decodeString(),
        distributionVersion = decodeString(),
        programmingLanguage = decodeNullableString(),
        authorName = decodeNullableString(),
        authorEmail = decodeNullableString()
    )
}

private fun Decoder.decodeAppRelease(): AppRelease {
    return AppRelease(
        type = decodeString(),
        identifier = decodeString(),
        versionCode = decodeLong(),
        versionName = decodeString(),
        targetSdkVersion = decodeString(),
        debug = decodeBoolean(),
        inheritStyle = decodeBoolean(),
        overrideStyle = decodeBoolean(),
        appStore = decodeNullableString()
    )
}

private fun Decoder.decodeEngagementData(): EngagementData {
    // FIXME: decode engagement data
    return EngagementData()
}

private fun Decoder.decodeEngagementManifest(): EngagementManifest {
    // FIXME: decode manifest
    return EngagementManifest()
}