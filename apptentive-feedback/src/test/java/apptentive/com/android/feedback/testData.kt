package apptentive.com.android.feedback

import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.criteria.DateTime
import apptentive.com.android.feedback.engagement.interactions.InteractionData
import apptentive.com.android.feedback.model.*

const val SDK_VERSION = "6.0.0"
const val API_VERSION = 9

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
    advertiserId = "advertiserId",
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
    version = SDK_VERSION,
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
    debug = true,
    inheritStyle = true,
    overrideStyle = false,
    appStore = "google"
)

val mockPerson = Person(
    id = "1234567890",
    email = "person@company.com",
    name = "First Last",
    facebookId = "facebook",
    phoneNumber = "555.555.5555",
    street = "123 Fake St",
    city = "Seattle",
    zip = "98121",
    country = "US",
    birthday = "1/1/1970",
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
                lastInvoked = DateTime(100)
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
                lastInvoked = DateTime(200)
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
                lastInvoked = DateTime(300)
            ),
            "222" to EngagementRecord(
                totalInvokes = 5,
                versionCodeLookup = mutableMapOf(
                    107L to 5L
                ),
                versionNameLookup = mutableMapOf(
                    "1.0.7" to 5L
                ),
                lastInvoked = DateTime(400)
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
                criteria = mapOf("interactions/id2/invokes/version_name" to mapOf("\$eq" to 1))
            ),
            InvocationData(
                interactionId = "id2",
                criteria = mapOf("interactions/id3/invokes/version_code" to mapOf("\$gt" to 0))
            ),
            InvocationData(
                interactionId = "id3"
            )
        )
    ),
    expiry = 1000.0
)