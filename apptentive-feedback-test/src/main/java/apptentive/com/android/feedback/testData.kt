package apptentive.com.android.feedback

import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.criteria.DateTime
import apptentive.com.android.feedback.engagement.interactions.InteractionData
import apptentive.com.android.feedback.engagement.interactions.InteractionResponse
import apptentive.com.android.feedback.engagement.interactions.InteractionResponseData
import apptentive.com.android.feedback.model.AppRelease
import apptentive.com.android.feedback.model.Conversation
import apptentive.com.android.feedback.model.CustomData
import apptentive.com.android.feedback.model.Device
import apptentive.com.android.feedback.model.EngagementData
import apptentive.com.android.feedback.model.EngagementManifest
import apptentive.com.android.feedback.model.EngagementRecord
import apptentive.com.android.feedback.model.EngagementRecords
import apptentive.com.android.feedback.model.IntegrationConfig
import apptentive.com.android.feedback.model.IntegrationConfigItem
import apptentive.com.android.feedback.model.InvocationData
import apptentive.com.android.feedback.model.Person
import apptentive.com.android.feedback.model.RandomSampling
import apptentive.com.android.feedback.model.SDK
import apptentive.com.android.feedback.model.VersionHistory
import apptentive.com.android.feedback.model.VersionHistoryItem
import apptentive.com.android.feedback.model.payloads.EventPayload

const val SDK_VERSION = "6.0.0"

val mockDevice = Device(
    osName = "Android",
    osVersion = "10",
    osBuild = "osBuild",
    osApiLevel = 29,
    manufacturer = "manufacturer",
    model = "model",
    board = "board",
    product = "product",
    brand = "brand",
    cpu = "cpu",
    device = "device",
    uuid = "uuid",
    buildType = "buildType",
    buildId = "buildId",
    carrier = "carrier",
    currentCarrier = "currentCarrier",
    networkType = "networkType",
    bootloaderVersion = "bootloaderVersion",
    radioVersion = "radioVersion",
    localeCountryCode = "localeCountryCode",
    localeLanguageCode = "localeLanguageCode",
    localeRaw = "localeRaw",
    utcOffset = 18000,
    customData = CustomData(content = mapOf(Pair("key", "value"))),
    integrationConfig = IntegrationConfig(
        apptentive = IntegrationConfigItem(
            contents = mapOf(
                Pair(
                    "apptentive_key",
                    "apptentive_value"
                )
            )
        ),
        amazonAwsSns = IntegrationConfigItem(
            contents = mapOf(
                Pair(
                    "amazon_key",
                    "amazon_value"
                )
            )
        ),
        urbanAirship = IntegrationConfigItem(
            contents = mapOf(
                Pair(
                    "urban_key",
                    "urban_value"
                )
            )
        )
    )
)

val mockSdk = SDK(
    version = Constants.SDK_VERSION,
    platform = "Android",
    distribution = "Default",
    distributionVersion = SDK_VERSION,
    programmingLanguage = "Kotlin",
    authorName = "Apptentive",
    authorEmail = "support@apptentive.com"
)

val mockAppRelease = AppRelease(
    type = "Android",
    identifier = "com.test.app",
    versionName = "1.0.0",
    versionCode = 1,
    targetSdkVersion = "29",
    minSdkVersion = "21",
    debug = true,
    inheritStyle = true,
    overrideStyle = false,
    appStore = "google"
)

val mockRandomSampling = RandomSampling(
    percents = mutableMapOf(
        "id1" to 0.1,
        "id2" to 99.9,
        "id3" to 50.0
    )
)

var mockPerson = Person(
    id = "1234567890",
    email = null,
    name = null,
    mParticleId = "mparticle",
    customData = CustomData(content = mapOf("person_key" to "person_value"))
)

val mockEngagementData = EngagementData(
    events = EngagementRecords(
        records = mutableMapOf(
            Event.local("event1") to EngagementRecord(
                totalInvokes = 3,
                versionCodeLookup = mutableMapOf(
                    100L to 2L,
                    101L to 1L
                ),
                versionNameLookup = mutableMapOf(
                    "1.0.0" to 2L,
                    "1.0.1" to 1L
                ),
                lastInvoked = DateTime(100.0)
            ),
            Event.internal("event2") to EngagementRecord(
                totalInvokes = 4,
                versionCodeLookup = mutableMapOf(
                    102L to 1L,
                    103L to 2L,
                    104L to 1L
                ),
                versionNameLookup = mutableMapOf(
                    "1.0.2" to 1L,
                    "1.0.3" to 2L,
                    "1.0.4" to 1L
                ),
                lastInvoked = DateTime(200.0)
            )
        )
    ),
    interactions = EngagementRecords(
        records = mutableMapOf(
            "111" to EngagementRecord(
                totalInvokes = 3,
                versionCodeLookup = mutableMapOf(
                    105L to 1L,
                    106L to 2L
                ),
                versionNameLookup = mutableMapOf(
                    "1.0.5" to 1L,
                    "1.0.6" to 2L
                ),
                lastInvoked = DateTime(300.0)
            ),
            "222" to EngagementRecord(
                totalInvokes = 5,
                versionCodeLookup = mutableMapOf(
                    107L to 5L
                ),
                versionNameLookup = mutableMapOf(
                    "1.0.7" to 5L
                ),
                lastInvoked = DateTime(400.0)
            )
        )
    ),
    interactionResponses = mutableMapOf(
        "id1" to InteractionResponseData(
            responses = mutableSetOf(InteractionResponse.IdResponse("aaa111")),
            record = EngagementRecord(
                totalInvokes = 1,
                versionCodeLookup = mutableMapOf(
                    111L to 1L
                ),
                versionNameLookup = mutableMapOf(
                    "1.1.1" to 1L
                ),
                lastInvoked = DateTime(100.0)
            )
        ),
        "id2" to InteractionResponseData(
            responses = mutableSetOf(InteractionResponse.LongResponse(222)),
            record = EngagementRecord(
                totalInvokes = 2,
                versionCodeLookup = mutableMapOf(
                    222L to 2L
                ),
                versionNameLookup = mutableMapOf(
                    "2.2.2" to 2L
                ),
                lastInvoked = DateTime(200.0)
            )
        ),
        "id3" to InteractionResponseData(
            responses = mutableSetOf(InteractionResponse.StringResponse("333")),
            record = EngagementRecord(
                totalInvokes = 3,
                versionCodeLookup = mutableMapOf(
                    333L to 3L
                ),
                versionNameLookup = mutableMapOf(
                    "3.3.3" to 3L
                ),
                lastInvoked = DateTime(300.0)
            )
        ),
        "id4" to InteractionResponseData(
            responses = mutableSetOf(InteractionResponse.OtherResponse("444", "ddd")),
            record = EngagementRecord(
                totalInvokes = 3,
                versionCodeLookup = mutableMapOf(
                    444L to 4L
                ),
                versionNameLookup = mutableMapOf(
                    "4.4.4" to 4L
                ),
                lastInvoked = DateTime(400.0)
            )
        ),
        "id5" to InteractionResponseData(
            responses = mutableSetOf(
                InteractionResponse.StringResponse("44444"),
                InteractionResponse.OtherResponse("fffff", "66666")
            ),
            record = EngagementRecord(
                totalInvokes = 4,
                versionCodeLookup = mutableMapOf(
                    444L to 4L
                ),
                versionNameLookup = mutableMapOf(
                    "4.4.4" to 4L
                ),
                lastInvoked = DateTime(400.0)
            )
        ),
    ),
    versionHistory = VersionHistory(
        items = listOf(
            VersionHistoryItem(
                timestamp = 1.0,
                versionCode = 100100,
                versionName = "1.0.0"
            ),
            VersionHistoryItem(
                timestamp = 2.0,
                versionCode = 200200,
                versionName = "2.0.0"
            )
        )
    )
)

val mockEngagementManifest = EngagementManifest(
    interactions = listOf(
        InteractionData("id1", "type1"),
        InteractionData("id2", "type2", displayType = "display_type"),
        InteractionData("id3", "type3", configuration = mapOf("key" to "value"))
    ),
    targets = mapOf(
        "local#app#event" to listOf(
            InvocationData(
                interactionId = "id1",
                criteria = mapOf("interactions/id2/invokes/version_name" to mapOf("\$eq" to 1.0))
            ),
            InvocationData(
                interactionId = "id2",
                criteria = mapOf("interactions/id3/invokes/version_code" to mapOf("\$gt" to 0.0))
            ),
            InvocationData(
                interactionId = "id3"
            )
        )
    ),
    expiry = 1000.0
)

var mockEventPayload = EventPayload(
    nonce = "nonce",
    label = "label",
    interactionId = "interactionId",
    data = mapOf<String, Any>(
        "key" to "value"
    ),
    customData = mapOf<String, Any>(
        "custom_key" to "custom_value"
    )
)

fun createMockConversation(
    localIdentifier: String = "localIdentifier",
    conversationToken: String? = null,
    conversationId: String? = null,
    device: Device? = null,
    person: Person? = null,
    sdk: SDK? = null,
    appRelease: AppRelease? = null,
    randomSampling: RandomSampling? = null,
    engagementData: EngagementData? = null,
    engagementManifest: EngagementManifest? = null
) = Conversation(
    localIdentifier = localIdentifier,
    conversationToken = conversationToken,
    conversationId = conversationId,
    device = device ?: mockDevice,
    person = person ?: mockPerson,
    sdk = sdk ?: mockSdk,
    appRelease = appRelease ?: mockAppRelease,
    randomSampling = randomSampling ?: mockRandomSampling,
    engagementData = engagementData ?: mockEngagementData,
    engagementManifest = engagementManifest ?: mockEngagementManifest
)
