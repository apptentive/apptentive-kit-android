package apptentive.com.android.feedback.model

import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.criteria.DateTime
import com.apptentive.android.sdk.conversation.LegacyAppRelease
import com.apptentive.android.sdk.conversation.LegacyConversationData
import com.apptentive.android.sdk.conversation.LegacyCustomData
import com.apptentive.android.sdk.conversation.LegacyDevice
import com.apptentive.android.sdk.conversation.LegacyEventData
import com.apptentive.android.sdk.conversation.LegacyEventRecord
import com.apptentive.android.sdk.conversation.LegacyIntegrationConfig
import com.apptentive.android.sdk.conversation.LegacyIntegrationConfigItem
import com.apptentive.android.sdk.conversation.LegacyPerson
import com.apptentive.android.sdk.conversation.LegacySdk
import com.apptentive.android.sdk.conversation.LegacyVersionHistory
import com.apptentive.android.sdk.conversation.LegacyVersionHistoryItem
import com.apptentive.android.sdk.conversation.toConversation
import com.google.common.truth.Truth
import org.junit.Test

class LegacyConversationConversionTest {
    @Test
    fun testConversion() {
        val original = createLegacyConversationData()
        val expected = createConversation()
        val actual = original.toConversation()
        Truth.assertThat(actual).isEqualTo(expected)
    }

    private fun createConversation(): Conversation {
        val localIdentifier = "localIdentifier"
        val conversationToken = "conversationToken"
        val conversationId = "conversationId"
        val device = createDevice(id = 1)
        val person = createPerson(id = 2)
        val sdk = createSDK()
        val appRelease = createAppRelease()
        val configuration = SDKStatus()
        val randomSampling = RandomSampling()
        val engagementData = createEngagementData()

        return Conversation(
            localIdentifier,
            conversationToken,
            conversationId,
            device,
            person,
            sdk,
            appRelease,
            configuration,
            randomSampling,
            engagementData
        )
    }

    private fun createEngagementData(): EngagementData {

        return EngagementData(
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
    }

    private fun createAppRelease(): AppRelease {
        val type = "type"
        val identifier = "identifier"
        val versionCode = 0
        val versionName = "versionName"
        val targetSdkVersion = "targetSdkVersion"
        val minSdkVersion = "0"
        val debug = false
        val inheritStyle = false
        val overrideStyle = false
        val appStore = "appStore"

        return AppRelease(
            type,
            identifier,
            versionCode.toLong(),
            versionName,
            targetSdkVersion,
            minSdkVersion,
            debug,
            inheritStyle,
            overrideStyle,
            appStore
        )
    }

    private fun createSDK(): SDK {
        val version = "version"
        val platform = "platform"
        val distribution = "distribution"
        val distributionVersion = "distributionVersion"
        val programmingLanguage = "programmingLanguage"
        val authorName = "authorName"
        val authorEmail = "authorEmail"

        return SDK(
            version,
            platform,
            distribution,
            distributionVersion,
            programmingLanguage,
            authorName,
            authorEmail
        )
    }

    private fun createPerson(id: Int): Person {
        val personId = "id-$id"
        val email = "email-$id"
        val name = "name-$id"
        val mParticleId = "mParticleId-$id"
        val customData = createCustomData(id)

        return Person(
            personId,
            email,
            name,
            mParticleId,
            customData
        )
    }

    private fun createDevice(id: Int): Device {
        val osName = "osName-$id"
        val osVersion = "osVersion-$id"
        val osBuild = "osBuild-$id"
        val osApiLevel = 0
        val manufacturer = "manufacturer-$id"
        val model = "model-$id"
        val board = "board-$id"
        val product = "product-$id"
        val brand = "brand-$id"
        val cpu = "cpu-$id"
        val device = "device-$id"
        val uuid = "uuid-$id"
        val buildType = "buildType-$id"
        val buildId = "buildId-$id"
        val carrier = "carrier-$id"
        val currentCarrier = "currentCarrier-$id"
        val networkType = "networkType-$id"
        val bootloaderVersion = "bootloaderVersion-$id"
        val radioVersion = "radioVersion-$id"
        val localeCountryCode = "localeCountryCode-$id"
        val localeLanguageCode = "localeLanguageCode-$id"
        val localeRaw = "localeRaw-$id"
        val utcOffset = 0
        val customData: CustomData = createCustomData(id)
        val integrationConfig: IntegrationConfig = createIntegrationConfig(id)

        return Device(
            osName,
            osVersion,
            osBuild,
            osApiLevel,
            manufacturer,
            model,
            board,
            product,
            brand,
            cpu,
            device,
            uuid,
            buildType,
            buildId,
            carrier,
            currentCarrier,
            networkType,
            bootloaderVersion,
            radioVersion,
            localeCountryCode,
            localeLanguageCode,
            localeRaw,
            utcOffset,
            customData,
            integrationConfig
        )
    }

    private fun createIntegrationConfig(id: Int): IntegrationConfig {
        return IntegrationConfig(
            apptentive = IntegrationConfigItem(
                contents = mapOf(
                    Pair(
                        "apptentive_key-$id",
                        "apptentive_value-$id"
                    )
                )
            ),
            amazonAwsSns = IntegrationConfigItem(
                contents = mapOf(
                    Pair(
                        "amazon_key-$id",
                        "amazon_value-$id"
                    )
                )
            ),
            urbanAirship = IntegrationConfigItem(
                contents = mapOf(
                    Pair(
                        "urban_key-$id",
                        "urban_value-$id"
                    )
                )
            ),
            parse = IntegrationConfigItem(
                contents = mapOf(
                    Pair(
                        "parse_key-$id",
                        "parse_value-$id"
                    )
                )
            )
        )
    }

    private fun createCustomData(id: Int): CustomData {
        return CustomData(content = mapOf(Pair("key-$id", "value-$id")))
    }

    private fun createLegacyConversationData(): LegacyConversationData {
        val localIdentifier = "localIdentifier"
        val conversationToken = "conversationToken"
        val conversationId = "conversationId"
        val device = createLegacyDevice(id = 1)
        val lastSentDevice = createLegacyDevice(id = 2)
        val person = createLegacyPerson(id = 2)
        val lastSentPerson = createLegacyPerson(id = 3)
        val sdk = createLegacySdk()
        val appRelease = createLegacyAppRelease()
        val eventData = createLegacyEventData()
        val lastSeenSdkVersion = "lastSeenSdkVersion"
        val versionHistory = createLegacyVersionHistory()
        val messageCenterFeatureUsed = true
        val messageCenterWhoCardPreviouslyDisplayed = false
        val messageCenterPendingMessage = "messageCenterPendingMessage"
        val messageCenterPendingAttachments = "messageCenterPendingAttachments"
        val targets = "targets"
        val interactions = "interactions"
        val interactionExpiration = 3.14

        return LegacyConversationData(
            localIdentifier,
            conversationToken,
            conversationId,
            device,
            lastSentDevice,
            person,
            lastSentPerson,
            sdk,
            appRelease,
            eventData,
            lastSeenSdkVersion,
            versionHistory,
            messageCenterFeatureUsed,
            messageCenterWhoCardPreviouslyDisplayed,
            messageCenterPendingMessage,
            messageCenterPendingAttachments,
            targets,
            interactions,
            interactionExpiration
        )
    }

    private fun createLegacyVersionHistory(): LegacyVersionHistory {
        val versionHistoryItemsList = listOf(
            LegacyVersionHistoryItem(1.0, 100100, "1.0.0"),
            LegacyVersionHistoryItem(2.0, 200200, "2.0.0")
        )
        val legacyVersionHistory = LegacyVersionHistory()
        legacyVersionHistory.versionHistoryItems = versionHistoryItemsList
        return legacyVersionHistory
    }

    private fun createLegacyEventData(): LegacyEventData {
        val events = createLegacyEventRecords()
        val interactions = createLegacyInteractionRecords()
        return LegacyEventData(events, interactions)
    }

    private fun createLegacyEventRecords(): Map<String, LegacyEventRecord> {
        val legacyEventRecordOne = LegacyEventRecord()
        legacyEventRecordOne.total = 3
        legacyEventRecordOne.versionCodes = mutableMapOf(
            100 to 2L,
            101 to 1L
        )
        legacyEventRecordOne.versionNames = mutableMapOf(
            "1.0.0" to 2L,
            "1.0.1" to 1L
        )
        legacyEventRecordOne.last = DateTime(100.0).seconds

        val legacyEventRecordTwo = LegacyEventRecord()
        legacyEventRecordTwo.total = 4
        legacyEventRecordTwo.versionCodes = mutableMapOf(
            102 to 1L,
            103 to 2L,
            104 to 1L
        )
        legacyEventRecordTwo.versionNames = mutableMapOf(
            "1.0.2" to 1L,
            "1.0.3" to 2L,
            "1.0.4" to 1L
        )
        legacyEventRecordTwo.last = DateTime(200.0).seconds

        return mapOf(
            Pair("local#app#event1", legacyEventRecordOne), Pair("com.apptentive#app#event2", legacyEventRecordTwo)
        )
    }

    private fun createLegacyInteractionRecords(): Map<String, LegacyEventRecord> {
        val legacyEventRecordOne = LegacyEventRecord()
        legacyEventRecordOne.total = 3
        legacyEventRecordOne.versionCodes = mutableMapOf(
            105 to 1L,
            106 to 2L
        )
        legacyEventRecordOne.versionNames = mutableMapOf(
            "1.0.5" to 1L,
            "1.0.6" to 2L
        )
        legacyEventRecordOne.last = DateTime(300.0).seconds

        val legacyEventRecordTwo = LegacyEventRecord()
        legacyEventRecordTwo.total = 5
        legacyEventRecordTwo.versionCodes = mutableMapOf(
            107 to 5L
        )
        legacyEventRecordTwo.versionNames = mutableMapOf(
            "1.0.7" to 5L
        )
        legacyEventRecordTwo.last = DateTime(400.0).seconds

        return mapOf(
            Pair("111", legacyEventRecordOne), Pair("222", legacyEventRecordTwo)
        )
    }

    private fun createLegacyAppRelease(): LegacyAppRelease {
        val appStore = "appStore"
        val debug = false
        val identifier = "identifier"
        val inheritStyle = false
        val overrideStyle = false
        val targetSdkVersion = "targetSdkVersion"
        val type = "type"
        val versionCode = 0
        val versionName = "versionName"
        return LegacyAppRelease(
            appStore,
            debug,
            identifier,
            inheritStyle,
            overrideStyle,
            targetSdkVersion,
            type,
            versionCode,
            versionName
        )
    }

    private fun createLegacySdk(): LegacySdk {
        val version = "version"
        val programmingLanguage = "programmingLanguage"
        val authorName = "authorName"
        val authorEmail = "authorEmail"
        val platform = "platform"
        val distribution = "distribution"
        val distributionVersion = "distributionVersion"
        return LegacySdk(
            version,
            programmingLanguage,
            authorName,
            authorEmail,
            platform,
            distribution,
            distributionVersion
        )
    }

    private fun createLegacyPerson(id: Int): LegacyPerson {
        val personId = "id-$id"
        val email = "email-$id"
        val name = "name-$id"
        val facebookId = "facebookId-$id"
        val phoneNumber = "phoneNumber-$id"
        val street = "street-$id"
        val city = "city-$id"
        val zip = "zip-$id"
        val country = "country-$id"
        val birthday = "birthday-$id"
        val mParticleId = "mParticleId-$id"
        val customData: LegacyCustomData? = createLegacyCustomData(id)

        return LegacyPerson(
            personId,
            email,
            name,
            facebookId,
            phoneNumber,
            street,
            city,
            zip,
            country,
            birthday,
            mParticleId,
            customData
        )
    }

    private fun createLegacyDevice(id: Int): LegacyDevice {
        val uuid = "uuid-$id"
        val osName = "osName-$id"
        val osVersion = "osVersion-$id"
        val osBuild = "osBuild-$id"
        val osApiLevel = 0
        val manufacturer = "manufacturer-$id"
        val model = "model-$id"
        val board = "board-$id"
        val product = "product-$id"
        val brand = "brand-$id"
        val cpu = "cpu-$id"
        val device = "device-$id"
        val carrier = "carrier-$id"
        val currentCarrier = "currentCarrier-$id"
        val networkType = "networkType-$id"
        val buildType = "buildType-$id"
        val buildId = "buildId-$id"
        val bootloaderVersion = "bootloaderVersion-$id"
        val radioVersion = "radioVersion-$id"
        val customData: LegacyCustomData? = createLegacyCustomData(id)
        val localeCountryCode = "localeCountryCode-$id"
        val localeLanguageCode = "localeLanguageCode-$id"
        val localeRaw = "localeRaw-$id"
        val utcOffset = "0"
        val integrationConfig: LegacyIntegrationConfig = createLegacyIntegrationConfig(id)

        return LegacyDevice(
            uuid,
            osName,
            osVersion,
            osBuild,
            osApiLevel,
            manufacturer,
            model,
            board,
            product,
            brand,
            cpu,
            device,
            carrier,
            currentCarrier,
            networkType,
            buildType,
            buildId,
            bootloaderVersion,
            radioVersion,
            customData,
            localeCountryCode,
            localeLanguageCode,
            localeRaw,
            utcOffset,
            integrationConfig
        )
    }

    private fun createLegacyCustomData(id: Int): LegacyCustomData {
        val legacyCustomData = LegacyCustomData()
        legacyCustomData["key-$id"] = "value-$id"
        return legacyCustomData
    }

    private fun createLegacyIntegrationConfig(id: Int): LegacyIntegrationConfig {
        val apptentive = createLegacyIntegrationConfigItemForApptentive(id)
        val amazonAwsSns = createLegacyIntegrationConfigItemForAmazonAWS(id)
        val urbanAirship = createLegacyIntegrationConfigItemForUrbanAirship(id)
        val parse = createLegacyIntegrationConfigItemForParse(id)

        return LegacyIntegrationConfig(
            apptentive,
            amazonAwsSns,
            urbanAirship,
            parse
        )
    }

    private fun createLegacyIntegrationConfigItemForParse(id: Int): LegacyIntegrationConfigItem {
        val legacyIntegrationConfigItem = LegacyIntegrationConfigItem()
        legacyIntegrationConfigItem.contents["parse_key-$id"] = "parse_value-$id"
        return legacyIntegrationConfigItem
    }

    private fun createLegacyIntegrationConfigItemForUrbanAirship(id: Int): LegacyIntegrationConfigItem {
        val legacyIntegrationConfigItem = LegacyIntegrationConfigItem()
        legacyIntegrationConfigItem.contents["urban_key-$id"] = "urban_value-$id"
        return legacyIntegrationConfigItem
    }

    private fun createLegacyIntegrationConfigItemForAmazonAWS(id: Int): LegacyIntegrationConfigItem {
        val legacyIntegrationConfigItem = LegacyIntegrationConfigItem()
        legacyIntegrationConfigItem.contents["amazon_key-$id"] = "amazon_value-$id"
        return legacyIntegrationConfigItem
    }

    private fun createLegacyIntegrationConfigItemForApptentive(id: Int): LegacyIntegrationConfigItem {
        val legacyIntegrationConfigItem = LegacyIntegrationConfigItem()
        legacyIntegrationConfigItem.contents["apptentive_key-$id"] = "apptentive_value-$id"
        return legacyIntegrationConfigItem
    }
}
