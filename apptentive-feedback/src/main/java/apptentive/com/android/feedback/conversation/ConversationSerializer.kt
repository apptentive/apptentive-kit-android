package apptentive.com.android.feedback.conversation

import apptentive.com.android.feedback.CONVERSATION
import apptentive.com.android.feedback.conversation.Serializers.conversationSerializer
import apptentive.com.android.feedback.conversation.Serializers.engagementManifestSerializer
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.criteria.DateTime
import apptentive.com.android.feedback.engagement.interactions.InteractionId
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
            conversationSerializer.encode(encoder, conversation)
        }
        if (shouldSaveManifest) {
            manifestFile.outputStream().use { stream ->
                val encoder = BinaryEncoder(DataOutputStream(stream))
                engagementManifestSerializer.encode(encoder, conversation.engagementManifest)
            }
            shouldSaveManifest = false
        }
    }

    override fun loadConversation(): Conversation? {
        if (conversationFile.exists()) {
            val conversation = conversationFile.inputStream().use { stream ->
                val decoder = BinaryDecoder(DataInputStream(stream))
                conversationSerializer.decode(decoder)
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
                    engagementManifestSerializer.decode(encoder)
                }
            } catch (e: Exception) {
                Log.e(CONVERSATION, "Exception while loading manifest file", e)
            }
        }
        return null
    }
}

// TODO: refactor this
internal object Serializers {
    val versionCodeSerializer = LongSerializer

    val versionNameSerializer = StringSerializer

    val interactionIdSerializer = StringSerializer

    val dateTimeSerializer: TypeSerializer<DateTime> by lazy {
        object : TypeSerializer<DateTime> {
            override fun encode(encoder: Encoder, value: DateTime) {
                encoder.encodeLong(value.seconds)
            }

            override fun decode(decoder: Decoder): DateTime {
                return DateTime(seconds = decoder.decodeLong())
            }
        }
    }

    val customDataSerializer: TypeSerializer<CustomData> by lazy {
        object : TypeSerializer<CustomData> {
            override fun encode(encoder: Encoder, value: CustomData) {
                encoder.encodeMap(value.content)
            }

            override fun decode(decoder: Decoder) = CustomData(content = decoder.decodeMap())
        }
    }

    val deviceSerializer: TypeSerializer<Device> by lazy {
        object : TypeSerializer<Device> {
            override fun encode(encoder: Encoder, value: Device) {
                encoder.encodeString(value.osName)
                encoder.encodeString(value.osVersion)
                encoder.encodeString(value.osBuild)
                encoder.encodeInt(value.osApiLevel)
                encoder.encodeString(value.manufacturer)
                encoder.encodeString(value.model)
                encoder.encodeString(value.board)
                encoder.encodeString(value.product)
                encoder.encodeString(value.brand)
                encoder.encodeString(value.cpu)
                encoder.encodeString(value.device)
                encoder.encodeString(value.uuid)
                encoder.encodeString(value.buildType)
                encoder.encodeString(value.buildId)
                encoder.encodeNullableString(value.carrier)
                encoder.encodeNullableString(value.currentCarrier)
                encoder.encodeNullableString(value.networkType)
                encoder.encodeNullableString(value.bootloaderVersion)
                encoder.encodeNullableString(value.radioVersion)
                encoder.encodeString(value.localeCountryCode)
                encoder.encodeString(value.localeLanguageCode)
                encoder.encodeString(value.localeRaw)
                encoder.encodeInt(value.utcOffset)
                encoder.encodeNullableString(value.advertiserId)
                customDataSerializer.encode(encoder, value.customData)
                encodeIntegrationConfigItem(encoder, value)
            }

            private fun encodeIntegrationConfigItem(encoder: Encoder, value: Device) {
                encodeNullableIntegrationConfigItem(encoder, value.integrationConfig.apptentive)
                encodeNullableIntegrationConfigItem(encoder, value.integrationConfig.amazonAwsSns)
                encodeNullableIntegrationConfigItem(encoder, value.integrationConfig.urbanAirship)
                encodeNullableIntegrationConfigItem(encoder, value.integrationConfig.parse)
            }

            private fun encodeNullableIntegrationConfigItem(
                encoder: Encoder,
                obj: IntegrationConfigItem?
            ) {
                encoder.encodeBoolean(obj != null)
                if (obj != null) {
                    encodeIntegrationConfigItem(encoder, obj)
                }
            }

            private fun encodeIntegrationConfigItem(encoder: Encoder, obj: IntegrationConfigItem) {
                encoder.encodeMap(obj.contents)
            }

            override fun decode(decoder: Decoder): Device {
                return Device(
                    osName = decoder.decodeString(),
                    osVersion = decoder.decodeString(),
                    osBuild = decoder.decodeString(),
                    osApiLevel = decoder.decodeInt(),
                    manufacturer = decoder.decodeString(),
                    model = decoder.decodeString(),
                    board = decoder.decodeString(),
                    product = decoder.decodeString(),
                    brand = decoder.decodeString(),
                    cpu = decoder.decodeString(),
                    device = decoder.decodeString(),
                    uuid = decoder.decodeString(),
                    buildType = decoder.decodeString(),
                    buildId = decoder.decodeString(),
                    carrier = decoder.decodeNullableString(),
                    currentCarrier = decoder.decodeNullableString(),
                    networkType = decoder.decodeNullableString(),
                    bootloaderVersion = decoder.decodeNullableString(),
                    radioVersion = decoder.decodeNullableString(),
                    localeCountryCode = decoder.decodeString(),
                    localeLanguageCode = decoder.decodeString(),
                    localeRaw = decoder.decodeString(),
                    utcOffset = decoder.decodeInt(),
                    advertiserId = decoder.decodeNullableString(),
                    customData = customDataSerializer.decode(decoder),
                    integrationConfig = decodeIntegrationConfig(decoder)
                )
            }

            private fun decodeIntegrationConfig(decoder: Decoder) =
                IntegrationConfig(
                    apptentive = decodeNullableIntegrationConfigItem(decoder),
                    amazonAwsSns = decodeNullableIntegrationConfigItem(decoder),
                    urbanAirship = decodeNullableIntegrationConfigItem(decoder),
                    parse = decodeNullableIntegrationConfigItem(decoder)
                )

            private fun decodeNullableIntegrationConfigItem(decoder: Decoder) =
                if (decoder.decodeBoolean()) decodeIntegrationConfigItem(decoder) else null

            private fun decodeIntegrationConfigItem(decoder: Decoder) =
                IntegrationConfigItem(contents = decoder.decodeMap())
        }
    }

    val personSerializer: TypeSerializer<Person> by lazy {
        object : TypeSerializer<Person> {
            override fun encode(encoder: Encoder, value: Person) {
                encoder.encodeNullableString(value.id)
                encoder.encodeNullableString(value.email)
                encoder.encodeNullableString(value.name)
                encoder.encodeNullableString(value.facebookId)
                encoder.encodeNullableString(value.phoneNumber)
                encoder.encodeNullableString(value.street)
                encoder.encodeNullableString(value.city)
                encoder.encodeNullableString(value.zip)
                encoder.encodeNullableString(value.country)
                encoder.encodeNullableString(value.birthday)
                encoder.encodeNullableString(value.mParticleId)
                customDataSerializer.encode(encoder, value.customData)
            }

            override fun decode(decoder: Decoder): Person {
                return Person(
                    id = decoder.decodeNullableString(),
                    email = decoder.decodeNullableString(),
                    name = decoder.decodeNullableString(),
                    facebookId = decoder.decodeNullableString(),
                    phoneNumber = decoder.decodeNullableString(),
                    street = decoder.decodeNullableString(),
                    city = decoder.decodeNullableString(),
                    zip = decoder.decodeNullableString(),
                    country = decoder.decodeNullableString(),
                    birthday = decoder.decodeNullableString(),
                    mParticleId = decoder.decodeNullableString(),
                    customData = customDataSerializer.decode(decoder)
                )
            }
        }
    }

    val sdkSerializer: TypeSerializer<SDK> by lazy {
        object : TypeSerializer<SDK> {
            override fun encode(encoder: Encoder, value: SDK) {
                encoder.encodeString(value.version)
                encoder.encodeString(value.platform)
                encoder.encodeString(value.distribution)
                encoder.encodeString(value.distributionVersion)
                encoder.encodeNullableString(value.programmingLanguage)
                encoder.encodeNullableString(value.authorName)
                encoder.encodeNullableString(value.authorEmail)
            }

            override fun decode(decoder: Decoder): SDK {
                return SDK(
                    version = decoder.decodeString(),
                    platform = decoder.decodeString(),
                    distribution = decoder.decodeString(),
                    distributionVersion = decoder.decodeString(),
                    programmingLanguage = decoder.decodeNullableString(),
                    authorName = decoder.decodeNullableString(),
                    authorEmail = decoder.decodeNullableString()
                )
            }
        }
    }

    val appReleaseSerializer: TypeSerializer<AppRelease> by lazy {
        object : TypeSerializer<AppRelease> {
            override fun encode(encoder: Encoder, value: AppRelease) {
                encoder.encodeString(value.type)
                encoder.encodeString(value.identifier)
                encoder.encodeLong(value.versionCode)
                encoder.encodeString(value.versionName)
                encoder.encodeString(value.targetSdkVersion)
                encoder.encodeBoolean(value.debug)
                encoder.encodeBoolean(value.inheritStyle)
                encoder.encodeBoolean(value.overrideStyle)
                encoder.encodeNullableString(value.appStore)
            }

            override fun decode(decoder: Decoder): AppRelease {
                return AppRelease(
                    type = decoder.decodeString(),
                    identifier = decoder.decodeString(),
                    versionCode = decoder.decodeLong(),
                    versionName = decoder.decodeString(),
                    targetSdkVersion = decoder.decodeString(),
                    debug = decoder.decodeBoolean(),
                    inheritStyle = decoder.decodeBoolean(),
                    overrideStyle = decoder.decodeBoolean(),
                    appStore = decoder.decodeNullableString()
                )
            }
        }
    }

    val engagementRecordSerializer: TypeSerializer<EngagementRecord> by lazy {
        object : TypeSerializer<EngagementRecord> {
            override fun encode(encoder: Encoder, value: EngagementRecord) {
                encoder.encodeLong(value.getTotalInvokes())
                encoder.encodeMap(
                    obj = value.versionCodes,
                    keyEncoder = versionCodeSerializer,
                    valueEncoder = LongSerializer
                )
                encoder.encodeMap(
                    obj = value.versionNames,
                    keyEncoder = versionNameSerializer,
                    valueEncoder = LongSerializer
                )
                dateTimeSerializer.encode(encoder, value.getLastInvoked())
            }

            override fun decode(decoder: Decoder): EngagementRecord {
                return EngagementRecord(
                    totalInvokes = decoder.decodeLong(),
                    versionCodeLookup = decoder.decodeMap(
                        keyDecoder = versionCodeSerializer,
                        valueDecoder = LongSerializer
                    ),
                    versionNameLookup = decoder.decodeMap(
                        keyDecoder =
                        versionNameSerializer, valueDecoder = LongSerializer
                    ),
                    lastInvoked = dateTimeSerializer.decode(decoder)
                )
            }
        }
    }

    val eventSerializer: TypeSerializer<Event> by lazy {
        object : TypeSerializer<Event> {
            override fun encode(encoder: Encoder, value: Event) =
                encoder.encodeString(value.fullName)

            override fun decode(decoder: Decoder) = Event.parse(decoder.decodeString())
        }
    }

    val engagementDataSerializer: TypeSerializer<EngagementData> by lazy {
        object : TypeSerializer<EngagementData> {
            override fun encode(encoder: Encoder, value: EngagementData) {
                encodeEventData(encoder, value)
                encodeInteractionData(encoder, value)
            }

            private fun encodeEventData(encoder: Encoder, obj: EngagementData) {
                encodeEngagementRecords(
                    encoder = encoder,
                    obj = obj.events,
                    keyEncoder = eventSerializer
                )
            }

            private fun encodeInteractionData(encoder: Encoder, obj: EngagementData) {
                encodeEngagementRecords(
                    encoder = encoder,
                    obj = obj.interactions,
                    keyEncoder = interactionIdSerializer
                )
            }

            private fun <Key : Any> encodeEngagementRecords(
                encoder: Encoder,
                obj: EngagementRecords<Key>,
                keyEncoder: TypeEncoder<Key>
            ) {
                encoder.encodeMap(
                    obj = obj.records,
                    keyEncoder = keyEncoder,
                    valueEncoder = engagementRecordSerializer
                )
            }

            override fun decode(decoder: Decoder): EngagementData {
                return EngagementData(
                    events = decodeEventRecords(decoder),
                    interactions = decodeInteractionRecords(decoder)
                )
            }

            private fun decodeEventRecords(decoder: Decoder): EngagementRecords<Event> {
                return decodeEngagementRecords(decoder, keyDecoder = eventSerializer)
            }

            private fun decodeInteractionRecords(decoder: Decoder): EngagementRecords<InteractionId> {
                return decodeEngagementRecords(decoder, keyDecoder = interactionIdSerializer)
            }

            private fun <Key : Any> decodeEngagementRecords(
                decoder: Decoder,
                keyDecoder: TypeDecoder<Key>
            ) = EngagementRecords(
                records = decoder.decodeMap(
                    keyDecoder = keyDecoder,
                    valueDecoder = engagementRecordSerializer
                )
            )
        }
    }

    val conversationSerializer: TypeSerializer<Conversation> by lazy {
        object : TypeSerializer<Conversation> {
            override fun encode(encoder: Encoder, value: Conversation) {
                encoder.encodeString(value.localIdentifier)
                encoder.encodeNullableString(value.conversationToken)
                encoder.encodeNullableString(value.conversationId)
                deviceSerializer.encode(encoder, value.device)
                personSerializer.encode(encoder, value.person)
                sdkSerializer.encode(encoder, value.sdk)
                appReleaseSerializer.encode(encoder, value.appRelease)
                engagementDataSerializer.encode(encoder, value.engagementData)
            }

            override fun decode(decoder: Decoder): Conversation {
                return Conversation(
                    localIdentifier = decoder.decodeString(),
                    conversationToken = decoder.decodeNullableString(),
                    conversationId = decoder.decodeNullableString(),
                    device = deviceSerializer.decode(decoder),
                    person = personSerializer.decode(decoder),
                    sdk = sdkSerializer.decode(decoder),
                    appRelease = appReleaseSerializer.decode(decoder),
                    engagementManifest = EngagementManifest(), // EngagementManifest is serialized separately
                    engagementData = engagementDataSerializer.decode(decoder)
                )
            }
        }
    }

    val engagementManifestSerializer: TypeSerializer<EngagementManifest> by lazy {
        object : TypeSerializer<EngagementManifest> {
            override fun encode(encoder: Encoder, value: EngagementManifest) {
                // FIXME: encode manifest
            }

            override fun decode(decoder: Decoder): EngagementManifest {
                // FIXME: decode manifest
                return EngagementManifest()
            }
        }
    }
}