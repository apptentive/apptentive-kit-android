package apptentive.com.android.feedback.conversation

import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.criteria.DateTime
import apptentive.com.android.feedback.engagement.interactions.InteractionId
import apptentive.com.android.feedback.engagement.interactions.InteractionResponse
import apptentive.com.android.feedback.engagement.interactions.InteractionResponseData
import apptentive.com.android.feedback.model.AppRelease
import apptentive.com.android.feedback.model.Configuration
import apptentive.com.android.feedback.model.Conversation
import apptentive.com.android.feedback.model.CustomData
import apptentive.com.android.feedback.model.Device
import apptentive.com.android.feedback.model.EngagementData
import apptentive.com.android.feedback.model.EngagementManifest
import apptentive.com.android.feedback.model.EngagementRecord
import apptentive.com.android.feedback.model.EngagementRecords
import apptentive.com.android.feedback.model.IntegrationConfig
import apptentive.com.android.feedback.model.IntegrationConfigItem
import apptentive.com.android.feedback.model.Person
import apptentive.com.android.feedback.model.RandomSampling
import apptentive.com.android.feedback.model.SDK
import apptentive.com.android.feedback.model.VersionHistory
import apptentive.com.android.feedback.model.VersionHistoryItem
import apptentive.com.android.serialization.Decoder
import apptentive.com.android.serialization.DoubleSerializer
import apptentive.com.android.serialization.Encoder
import apptentive.com.android.serialization.LongSerializer
import apptentive.com.android.serialization.StringSerializer
import apptentive.com.android.serialization.TypeDecoder
import apptentive.com.android.serialization.TypeEncoder
import apptentive.com.android.serialization.TypeSerializer
import apptentive.com.android.serialization.decodeList
import apptentive.com.android.serialization.decodeMap
import apptentive.com.android.serialization.decodeNullableString
import apptentive.com.android.serialization.decodeSet
import apptentive.com.android.serialization.encodeList
import apptentive.com.android.serialization.encodeMap
import apptentive.com.android.serialization.encodeNullableString
import apptentive.com.android.serialization.encodeSet
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.MIGRATION

internal object DefaultSerializers {
    val versionCodeSerializer = LongSerializer

    val versionNameSerializer = StringSerializer

    val interactionIdSerializer = StringSerializer

    val dateTimeSerializer: TypeSerializer<DateTime> by lazy {
        object : TypeSerializer<DateTime> {
            override fun encode(encoder: Encoder, value: DateTime) {
                encoder.encodeDouble(value.seconds)
            }

            override fun decode(decoder: Decoder): DateTime {
                return DateTime(seconds = decoder.decodeDouble())
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
                encoder.encodeNullableString(value.mParticleId)
                customDataSerializer.encode(encoder, value.customData)
            }

            override fun decode(decoder: Decoder): Person {
                return Person(
                    id = decoder.decodeNullableString(),
                    email = decoder.decodeNullableString(),
                    name = decoder.decodeNullableString(),
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
                encoder.encodeNullableString(value.distribution)
                encoder.encodeNullableString(value.distributionVersion)
                encoder.encodeNullableString(value.programmingLanguage)
                encoder.encodeNullableString(value.authorName)
                encoder.encodeNullableString(value.authorEmail)
            }

            override fun decode(decoder: Decoder): SDK {
                return SDK(
                    version = decoder.decodeString(),
                    platform = decoder.decodeString(),
                    distribution = decoder.decodeNullableString(),
                    distributionVersion = decoder.decodeNullableString(),
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
                encoder.encodeString(value.minSdkVersion)
                encoder.encodeBoolean(value.debug)
                encoder.encodeBoolean(value.inheritStyle)
                encoder.encodeBoolean(value.overrideStyle)
                encoder.encodeNullableString(value.appStore)
                encoder.encodeNullableString(value.customAppStoreURL)
            }

            override fun decode(decoder: Decoder): AppRelease {
                return AppRelease(
                    type = decoder.decodeString(),
                    identifier = decoder.decodeString(),
                    versionCode = decoder.decodeLong(),
                    versionName = decoder.decodeString(),
                    targetSdkVersion = decoder.decodeString(),
                    minSdkVersion = decoder.decodeString(),
                    debug = decoder.decodeBoolean(),
                    inheritStyle = decoder.decodeBoolean(),
                    overrideStyle = decoder.decodeBoolean(),
                    appStore = decoder.decodeNullableString(),
                    customAppStoreURL = decoder.decodeNullableString()
                )
            }
        }
    }

    val configurationSerializer: TypeSerializer<Configuration> by lazy {
        object : TypeSerializer<Configuration> {
            override fun encode(encoder: Encoder, value: Configuration) {
                encoder.encodeDouble(value.expiry)
                messageCenterConfigurationSerializer.encode(encoder, value.messageCenter)
            }

            override fun decode(decoder: Decoder): Configuration {
                return Configuration(
                    expiry = decoder.decodeDouble(),
                    messageCenter = messageCenterConfigurationSerializer.decode(decoder)
                )
            }
        }
    }

    val messageCenterConfigurationSerializer: TypeSerializer<Configuration.MessageCenter> by lazy {
        object : TypeSerializer<Configuration.MessageCenter> {
            override fun encode(encoder: Encoder, value: Configuration.MessageCenter) {
                encoder.encodeDouble(value.fgPoll)
                encoder.encodeDouble(value.bgPoll)
            }

            override fun decode(decoder: Decoder): Configuration.MessageCenter {
                return Configuration.MessageCenter(
                    fgPoll = decoder.decodeDouble(),
                    bgPoll = decoder.decodeDouble()
                )
            }
        }
    }

    val randomSamplingSerializer: TypeSerializer<RandomSampling> by lazy {
        object : TypeSerializer<RandomSampling> {
            override fun encode(encoder: Encoder, value: RandomSampling) {
                encoder.encodeMap(
                    obj = value.percents,
                    keyEncoder = interactionIdSerializer,
                    valueEncoder = DoubleSerializer
                )
            }

            override fun decode(decoder: Decoder): RandomSampling {
                return RandomSampling(
                    percents = decoder.decodeMap(
                        keyDecoder = interactionIdSerializer,
                        valueDecoder = DoubleSerializer
                    )
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
                        keyDecoder = versionNameSerializer,
                        valueDecoder = LongSerializer
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

    val interactionResponseDataSerializer: TypeSerializer<InteractionResponseData> by lazy {
        object : TypeSerializer<InteractionResponseData> {
            override fun encode(encoder: Encoder, value: InteractionResponseData) {
                encoder.encodeSet(
                    obj = value.responses,
                    valueEncoder = interactionResponseSerializer
                )
                engagementRecordSerializer.encode(encoder, value.record)
            }

            override fun decode(decoder: Decoder): InteractionResponseData {
                return InteractionResponseData(
                    responses = decoder.decodeSet(interactionResponseSerializer),
                    record = engagementRecordSerializer.decode(decoder),
                )
            }
        }
    }

    val interactionResponseSerializer: TypeSerializer<InteractionResponse> by lazy {
        object : TypeSerializer<InteractionResponse> {
            override fun encode(encoder: Encoder, value: InteractionResponse) {
                val responseName = value::class.java.name
                encoder.encodeString(responseName)

                when (responseName) {
                    InteractionResponse.IdResponse::class.java.name -> {
                        value as InteractionResponse.IdResponse
                        encoder.encodeString(value.id)
                    }
                    InteractionResponse.LongResponse::class.java.name -> {
                        value as InteractionResponse.LongResponse
                        encoder.encodeLong(value.response)
                    }
                    InteractionResponse.StringResponse::class.java.name -> {
                        value as InteractionResponse.StringResponse
                        encoder.encodeString(value.response)
                    }
                    InteractionResponse.OtherResponse::class.java.name -> {
                        value as InteractionResponse.OtherResponse
                        encoder.encodeNullableString(value.id)
                        encoder.encodeNullableString(value.response)
                    }
                }
            }

            override fun decode(decoder: Decoder): InteractionResponse {
                val responseName = decoder.decodeString()
                val recoveredResponse = recoverResponse(responseName)

                return when {
                    responseName == InteractionResponse.IdResponse::class.java.name ||
                        recoveredResponse == InteractionResponse.IdResponse::class.java.name -> {
                        InteractionResponse.IdResponse(decoder.decodeString())
                    }
                    responseName == InteractionResponse.LongResponse::class.java.name ||
                        recoveredResponse == InteractionResponse.LongResponse::class.java.name -> {
                        InteractionResponse.LongResponse(decoder.decodeLong())
                    }
                    responseName == InteractionResponse.StringResponse::class.java.name ||
                        recoveredResponse == InteractionResponse.StringResponse::class.java.name -> {
                        InteractionResponse.StringResponse(decoder.decodeString())
                    }
                    responseName == InteractionResponse.OtherResponse::class.java.name ||
                        recoveredResponse == InteractionResponse.OtherResponse::class.java.name -> {
                        InteractionResponse.OtherResponse(
                            id = decoder.decodeNullableString(),
                            response = decoder.decodeNullableString()
                        )
                    }
                    else -> throw java.lang.Exception("Unknown InteractionResponse type: $responseName")
                }
            }

            // Class names from 6.0.X are not saved from minification.
            // This is a backup to handle that case assuming the class names are in the same order.
            private fun recoverResponse(responseName: String): String = when (responseName.last()) {
                'a' -> {
                    Log.d(MIGRATION, "Decoding interaction response: $responseName. Recovered as IdResponse")
                    InteractionResponse.IdResponse::class.java.name
                }

                'b' -> {
                    Log.d(MIGRATION, "Decoding interaction response: $responseName. Recovered as LongResponse")
                    InteractionResponse.LongResponse::class.java.name
                }

                'd' -> {
                    Log.d(MIGRATION, "Decoding interaction response: $responseName. Recovered as StringResponse")
                    InteractionResponse.StringResponse::class.java.name
                }

                'c' -> {
                    Log.d(MIGRATION, "Decoding interaction response: $responseName. Recovered as OtherResponse")
                    InteractionResponse.OtherResponse::class.java.name
                }

                else -> "Unknown or Backup not needed"
            }
        }
    }

    val engagementDataSerializer: TypeSerializer<EngagementData> by lazy {
        object : TypeSerializer<EngagementData> {
            override fun encode(encoder: Encoder, value: EngagementData) {
                encodeEventData(encoder, value.events)
                encodeInteractionData(encoder, value.interactions)
                encodeInteractionResponsesData(encoder, value.interactionResponses)
                encodeVersionHistory(encoder, value.versionHistory)
            }

            private fun encodeEventData(encoder: Encoder, events: EngagementRecords<Event>) =
                encodeEngagementRecords(
                    encoder = encoder,
                    obj = events,
                    keyEncoder = eventSerializer
                )

            private fun encodeInteractionData(encoder: Encoder, interactions: EngagementRecords<InteractionId>) =
                encodeEngagementRecords(
                    encoder = encoder,
                    obj = interactions,
                    keyEncoder = interactionIdSerializer
                )

            private fun encodeInteractionResponsesData(
                encoder: Encoder,
                interactionResponses: Map<InteractionId, InteractionResponseData>
            ) {
                encoder.encodeMap(
                    obj = interactionResponses,
                    keyEncoder = interactionIdSerializer,
                    valueEncoder = interactionResponseDataSerializer
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

            private fun encodeVersionHistory(encoder: Encoder, versionHistory: VersionHistory) =
                encoder.encodeList(versionHistory.items) { item ->
                    encodeDouble(item.timestamp)
                    encodeLong(item.versionCode)
                    encodeString(item.versionName)
                }

            override fun decode(decoder: Decoder): EngagementData {
                val events = decodeEventRecords(decoder)
                val interactions = decodeInteractionRecords(decoder)

                return try {
                    EngagementData(
                        events = events,
                        interactions = interactions,
                        interactionResponses = decodeInteractionResponsesRecords(decoder),
                        versionHistory = decodeVersionHistory(decoder)
                    )
                } catch (e: Exception) {
                    Log.e(MIGRATION, "Failed to decode InteractionResponses. Skipping.", e)
                    EngagementData(events, interactions)
                }
            }

            private fun decodeEventRecords(decoder: Decoder): EngagementRecords<Event> {
                return decodeEngagementRecords(decoder, keyDecoder = eventSerializer)
            }

            private fun decodeInteractionRecords(decoder: Decoder): EngagementRecords<InteractionId> {
                return decodeEngagementRecords(decoder, keyDecoder = interactionIdSerializer)
            }

            private fun decodeInteractionResponsesRecords(decoder: Decoder): MutableMap<InteractionId, InteractionResponseData> {
                return decoder.decodeMap(
                    keyDecoder = interactionIdSerializer,
                    valueDecoder = interactionResponseDataSerializer
                )
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

            private fun decodeVersionHistory(decoder: Decoder) = VersionHistory(
                items = decoder.decodeList {
                    VersionHistoryItem(
                        timestamp = decodeDouble(),
                        versionCode = decodeLong(),
                        versionName = decodeString()
                    )
                }
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
                configurationSerializer.encode(encoder, value.configuration)
                randomSamplingSerializer.encode(encoder, value.randomSampling)
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
                    configuration = configurationSerializer.decode(decoder),
                    randomSampling = randomSamplingSerializer.decode(decoder),
                    engagementManifest = EngagementManifest(), // EngagementManifest is serialized separately
                    engagementData = engagementDataSerializer.decode(decoder)
                )
            }
        }
    }
}
