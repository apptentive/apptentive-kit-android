package com.apptentive.android.sdk.conversation

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import apptentive.com.android.DependencyProviderRule
import apptentive.com.android.TestCase
import apptentive.com.android.feedback.Constants
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.criteria.DateTime
import apptentive.com.android.feedback.model.AppRelease
import apptentive.com.android.feedback.model.Conversation
import apptentive.com.android.feedback.model.CustomData
import apptentive.com.android.feedback.model.Device
import apptentive.com.android.feedback.model.EngagementData
import apptentive.com.android.feedback.model.EngagementManifest
import apptentive.com.android.feedback.model.EngagementRecord
import apptentive.com.android.feedback.model.EngagementRecords
import apptentive.com.android.feedback.model.Person
import apptentive.com.android.feedback.model.SDK
import apptentive.com.android.feedback.model.VersionHistory
import apptentive.com.android.feedback.model.VersionHistoryItem
import apptentive.com.android.feedback.utils.SensitiveDataUtils
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogLevel
import com.apptentive.android.sdk.encryption.EncryptionFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File

class LegacyConversationManagerTest : TestCase() {
    @get:Rule
    override val dependencyRule = DependencyProviderRule(true)
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Before
    fun before() {
        Log.logLevel = LogLevel.Verbose
        // clear up device storage
        MigrationTestUtils.clearDeviceStorage(context)
    }

    @Test
    fun testMigrationFrom400() = testMigration("4.0.0")

    @Test
    fun testSensitiveDataToStringRedactedFrom400Migration() = testSensitiveDataRedactedToString("4.0.0")

    @Test
    fun testSensitiveDataValueNotRedactedFrom400Migration() = testActualValuesNotRedacted("4.0.0")

    @Test
    fun testMigrationFrom562() = testMigration("5.6.2")

    @Test
    fun testSensitiveDataToStringRedactedFrom562Migration() = testSensitiveDataRedactedToString("5.6.2")

    @Test
    fun testSensitiveDataValueNotRedactedFrom562Migration() = testActualValuesNotRedacted("5.6.2")

    @Test
    fun testMigrationFrom562LoggedInConversation() = testMigration("5.6.2-login")

    @Test
    fun testSensitiveDataToStringRedactedFrom562LoggedInMigration() = testSensitiveDataRedactedToString("5.6.2-login")

    @Test
    fun testSensitiveDataValueNotRedactedFrom562LoggedInMigration() = testActualValuesNotRedacted("5.6.2-login")

    @Test
    fun testMigrationFrom570() = testMigration("5.7.0")

    @Test
    fun testSensitiveDataToStringRedactedFrom570Migration() = testSensitiveDataRedactedToString("5.7.0")

    @Test
    fun testSensitiveDataValueNotRedactedFrom570Migration() = testActualValuesNotRedacted("5.7.0")

    @Test
    fun testMigrationFrom570LoggedIn() = testMigration("5.7.0-login")

    @Test
    fun testSensitiveDataToStringRedactedFrom570LoggedInMigration() = testSensitiveDataRedactedToString("5.7.0-login")

    @Test
    fun testSensitiveDataValueNotRedactedFrom570LoggedInMigration() = testActualValuesNotRedacted("5.7.0-login")

    @Test
    fun testMigrationFrom571() = testMigration("5.7.1")

    @Test
    fun testSensitiveDataToStringRedactedFrom571Migration() = testSensitiveDataRedactedToString("5.7.1")

    @Test
    fun testSensitiveDataValueNotRedactedFrom571Migration() = testActualValuesNotRedacted("5.7.1")

    @Test
    fun testMigrationFrom571LoggedIn() = testMigration("5.7.1-login")

    @Test
    fun testSensitiveDataToStringRedactedFrom571LoggedInMigration() = testSensitiveDataRedactedToString("5.7.1-login")

    @Test
    fun testSensitiveDataValueNotRedactedFrom571LoggedInMigration() = testActualValuesNotRedacted("5.7.1-login")

    @Test
    fun testMigrationFrom584() = testMigration("5.8.4")

    @Test
    fun testSensitiveDataToStringRedactedFrom584Migration() = testSensitiveDataRedactedToString("5.8.4")

    @Test
    fun testSensitiveDataValueNotRedactedFrom584Migration() = testActualValuesNotRedacted("5.8.4")

    @Test
    fun testMigrationFrom584LoggedIn() = testMigration("5.8.4-login")

    @Test
    fun testSensitiveDataToStringRedactedFrom584LoggedInMigration() = testSensitiveDataRedactedToString("5.8.4-login")

    @Test
    fun testSensitiveDataValueNotRedactedFrom584LoggedInMigration() = testActualValuesNotRedacted("5.8.4-login")

    private fun testMigration(path: String) {
        SensitiveDataUtils.shouldSanitizeLogMessages = false
        pushFiles(path)

        val encryption = EncryptionFactory.NULL // it's impossible to automatically test encrypted storage since KeyStore does not allow exporting keys
        val manager = DefaultLegacyConversationManager(context, encryption)
        val legacyData = manager.loadLegacyConversationData()
            ?: throw AssertionError("Unable to load legacy conversation")

        val expected = createExpectedConversation(path)
        val actual = legacyData.toConversation()

        assertEquals(expected, actual)
    }

    private fun testSensitiveDataRedactedToString(path: String) {
        SensitiveDataUtils.shouldSanitizeLogMessages = true

        pushFiles(path)

        val encryption = EncryptionFactory.NULL // it's impossible to automatically test encrypted storage since KeyStore does not allow exporting keys
        val manager = DefaultLegacyConversationManager(context, encryption)
        val legacyData = manager.loadLegacyConversationData()
            ?: throw AssertionError("Unable to load legacy conversation")

        val actual = legacyData.toConversation()

        val redactedItems = mutableListOf<Pair<String, String>>()

        // Device redacted items
        redactedItems.add(Pair(actual.device.toString(), "custom_data"))

        // Person redacted items
        if (actual.person.mParticleId != null) redactedItems.add(Pair(actual.person.toString(), "m_particle_id"))
        redactedItems.add(Pair(actual.person.toString(), "custom_data"))

        // SDK redacted items
        if (actual.sdk.authorName != null) redactedItems.add(Pair(actual.sdk.toString(), "author_name"))
        if (actual.sdk.authorEmail != null) redactedItems.add(Pair(actual.sdk.toString(), "author_email"))

        redactedItems.forEach {
            assertTrue(it.first.contains("\"${it.second}\":\"${Constants.REDACTED_DATA}\""))
        }
    }

    private fun testActualValuesNotRedacted(path: String) {
        SensitiveDataUtils.shouldSanitizeLogMessages = true

        pushFiles(path)

        val encryption = EncryptionFactory.NULL // it's impossible to automatically test encrypted storage since KeyStore does not allow exporting keys
        val manager = DefaultLegacyConversationManager(context, encryption)
        val legacyData = manager.loadLegacyConversationData()
            ?: throw AssertionError("Unable to load legacy conversation")

        val expected = createExpectedConversation(path)
        val actual = legacyData.toConversation()

        val expectedActualItems = listOf(
            Pair(expected.device.customData, actual.device.customData),
            Pair(expected.person.mParticleId, actual.person.mParticleId),
            Pair(expected.person.customData, actual.person.customData),
            Pair(expected.sdk.authorName, actual.sdk.authorName),
            Pair(expected.sdk.authorEmail, actual.sdk.authorEmail)
        )

        expectedActualItems.forEach {
            assertNotEquals(Constants.REDACTED_DATA, it.first) // Sanity check
            assertNotEquals(Constants.REDACTED_DATA, it.second) // Sanity check
            assertEquals(it.first, it.second)
        }
    }

    private fun createExpectedConversation(path: String): Conversation {
        if (path == "4.0.0") {
            return Conversation(
                localIdentifier = "1c1042b6-8522-4952-b336-50d3b1807877",
                conversationToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJ0eXBlIjoiYW5vbiIsImlzcyI6ImFwcHRlbnRpdmUiLCJzdWIiOiI2MGIyYzg0ODRlYjAyYTZiNjUwZDRmZGMiLCJhcHBfaWQiOiI1MjFmOWJiYTY4ZTI3NThiNmQwMDA5OGQiLCJpYXQiOjE2MjIzMjk0MTZ9.x9u2nHHGLEEFRQOlo5XOwiSVjJ89how-ThKuITv4r0cXC2Lu7U-11e0MfKdModaZ6uppv4VzXF-MnLeLa64Tug",
                conversationId = "60b2c8484eb02a6b650d4fdc",
                device = Device(
                    osName = "Android",
                    osVersion = "9",
                    osBuild = "5124027",
                    osApiLevel = 28,
                    manufacturer = "Google",
                    model = "Android SDK built for x86_64",
                    board = "goldfish_x86_64",
                    product = "sdk_gphone_x86_64",
                    brand = "google",
                    cpu = "x86_64",
                    device = "generic_x86_64",
                    uuid = "cc736039c35ef28b",
                    buildType = "user",
                    buildId = "PSR1.180720.075",
                    carrier = "Android",
                    currentCarrier = "Android",
                    networkType = "LTE",
                    bootloaderVersion = "unknown",
                    radioVersion = "1.0.0.0",
                    localeCountryCode = "US",
                    localeLanguageCode = "en",
                    localeRaw = "en_US",
                    utcOffset = -18000,
                    customData = CustomData(
                        content = mapOf(
                            "device-int" to 20,
                            "device-bool" to false,
                            "device-str" to "device"
                        )
                    )
                ),
                person = Person(
                    id = "60b2c8484eb02a6b650d4fdb",
                    email = "person@company.com",
                    name = "First Second",
                    customData = CustomData(
                        content = mapOf(
                            "person-int" to 10,
                            "person-bool" to true,
                            "person-str" to "person"
                        )
                    )
                ),
                sdk = SDK(
                    version = "4.0.0",
                    platform = "Android"
                ),
                appRelease = AppRelease(
                    type = "android",
                    identifier = "apptentive.com.android.feedback.test",
                    versionCode = 100000,
                    versionName = "1.0.0",
                    targetSdkVersion = "30",
                    minSdkVersion = "0",
                    debug = false,
                    inheritStyle = false,
                    overrideStyle = false,
                    appStore = null
                ),
                engagementData = EngagementData(
                    events = EngagementRecords(
                        records = mutableMapOf(
                            Event.parse("com.apptentive#app#launch") to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.622329412578E9)
                            ),
                            Event.parse("local#app#love_dialog_test") to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.622329453678E9)
                            )
                        )
                    ),
                    interactions = EngagementRecords(),
                    versionHistory = VersionHistory(
                        items = listOf(
                            VersionHistoryItem(
                                timestamp = 1.622329411908E9,
                                versionCode = 100000,
                                versionName = "1.0.0"
                            )
                        )
                    )
                ),
                engagementManifest = EngagementManifest()
            )
        }
        if (path == "5.6.2") {
            return Conversation(
                localIdentifier = "aaaea07c-f3d9-4554-8dca-180c217744a5",
                conversationToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJ0eXBlIjoiYW5vbiIsImlzcyI6ImFwcHRlbnRpdmUiLCJzdWIiOiI2MGIyY2ZlZTZhNzk1NDZjMGQyODE4ZWMiLCJhcHBfaWQiOiI1MjFmOWJiYTY4ZTI3NThiNmQwMDA5OGQiLCJpYXQiOjE2MjIzMzEzNzR9.Oytihs3LJFsvo1fujmVRBMk4S95FxGQkwvUeDg6v4ZmXr_CL0mTVcytRf__5CU-yVRqzWE_QpumlaXVOtHbD-Q",
                conversationId = "60b2cfee6a79546c0d2818ec",
                device = Device(
                    osName = "Android",
                    osVersion = "9",
                    osBuild = "5124027",
                    osApiLevel = 28,
                    manufacturer = "Google",
                    model = "Android SDK built for x86_64",
                    board = "goldfish_x86_64",
                    product = "sdk_gphone_x86_64",
                    brand = "google",
                    cpu = "x86_64",
                    device = "generic_x86_64",
                    uuid = "cc736039c35ef28b",
                    buildType = "user",
                    buildId = "PSR1.180720.075",
                    carrier = "Android",
                    currentCarrier = "Android",
                    networkType = "LTE",
                    bootloaderVersion = "unknown",
                    radioVersion = "1.0.0.0",
                    localeCountryCode = "US",
                    localeLanguageCode = "en",
                    localeRaw = "en_US",
                    utcOffset = -18000,
                    customData = CustomData(
                        content = mapOf(
                            "device-int" to 20,
                            "device-bool" to false,
                            "device-str" to "device"
                        )
                    )
                ),
                person = Person(
                    id = "60b2cfee6a79546c0d2818eb",
                    email = "person@company.com",
                    name = "First Second",
                    customData = CustomData(
                        content = mapOf(
                            "person-int" to 10,
                            "person-bool" to true,
                            "person-str" to "person"
                        )
                    )
                ),
                sdk = SDK(
                    version = "5.6.2",
                    platform = "Android",
                    distribution = "default",
                    distributionVersion = "5.6.2"
                ),
                appRelease = AppRelease(
                    type = "android",
                    identifier = "apptentive.com.android.feedback.test",
                    versionCode = 100000,
                    versionName = "1.0.0",
                    targetSdkVersion = "30",
                    minSdkVersion = "0",
                    debug = true,
                    inheritStyle = false,
                    overrideStyle = false,
                    appStore = null
                ),
                engagementData = EngagementData(
                    events = EngagementRecords(
                        records = mutableMapOf(
                            Event.parse("com.apptentive#app#launch") to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.622331369858E9)
                            ),
                            Event.parse("local#app#love_dialog_test") to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.622331375906E9)
                            ),
                            Event.parse("com.apptentive#EnjoymentDialog#launch") to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.622331376478E9)
                            )
                        )
                    ),
                    interactions = EngagementRecords(
                        records = mutableMapOf(
                            "55c94046a71b52ea570054d9" to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.62233137605E9)
                            )
                        )
                    ),
                    versionHistory = VersionHistory(
                        items = listOf(
                            VersionHistoryItem(
                                timestamp = 1.622331370573E9,
                                versionCode = 100000,
                                versionName = "1.0.0"
                            )
                        )
                    )
                ),
                engagementManifest = EngagementManifest()
            )
        }
        if (path == "5.6.2-login") {
            return Conversation(
                localIdentifier = "1524b2a8-04c5-47d1-80a7-aeea05773eb9",
                conversationToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJ0eXBlIjoidXNlciIsInN1YiI6IlVzZXIiLCJpc3MiOiJDbGllbnRUZWFtIiwiaWF0IjoxNjIyNDE1ODU0LCJleHAiOjE2MjUwMDc4NTR9.DKVjYprRPm3qEyWIfxjJfLrbXinE-ful6QjKF0jVZ0mMhqbgAeP-2om6EMlm3nfJGRlbsYYBpC9NZs-6WJz0GQ",
                conversationId = "60b419ebe7e8116d9b0ef67e",
                device = Device(
                    osName = "Android",
                    osVersion = "9",
                    osBuild = "5124027",
                    osApiLevel = 28,
                    manufacturer = "Google",
                    model = "Android SDK built for x86_64",
                    board = "goldfish_x86_64",
                    product = "sdk_gphone_x86_64",
                    brand = "google",
                    cpu = "x86_64",
                    device = "generic_x86_64",
                    uuid = "cc736039c35ef28b",
                    buildType = "user",
                    buildId = "PSR1.180720.075",
                    carrier = "Android",
                    currentCarrier = "Android",
                    networkType = "LTE",
                    bootloaderVersion = "unknown",
                    radioVersion = "1.0.0.0",
                    localeCountryCode = "US",
                    localeLanguageCode = "en",
                    localeRaw = "en_US",
                    utcOffset = -18000,
                    customData = CustomData(
                        content = mapOf(
                            "device-int" to 20,
                            "device-bool" to false,
                            "device-str" to "device"
                        )
                    )
                ),
                person = Person(
                    id = "60b419ebe7e8116d9b0ef67d",
                    email = "person@company.com",
                    name = "First Second",
                    customData = CustomData(
                        content = mapOf(
                            "person-int" to 10,
                            "person-bool" to true,
                            "person-str" to "person"
                        )
                    )
                ),
                sdk = SDK(
                    version = "5.6.2",
                    platform = "Android",
                    distribution = "default",
                    distributionVersion = "5.6.2"
                ),
                appRelease = AppRelease(
                    type = "android",
                    identifier = "apptentive.com.android.feedback.test",
                    versionCode = 100000,
                    versionName = "1.0.0",
                    targetSdkVersion = "30",
                    minSdkVersion = "0",
                    debug = true,
                    inheritStyle = false,
                    overrideStyle = false,
                    appStore = null
                ),
                engagementData = EngagementData(
                    events = EngagementRecords(
                        records = mutableMapOf(
                            Event.parse("com.apptentive#app#launch") to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.622415846201E9)
                            ),
                            Event.parse("local#app#love_dialog_test") to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.622415874126E9)
                            ),
                            Event.parse("com.apptentive#app#login") to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.622415855878E9)
                            ),
                            Event.parse("com.apptentive#EnjoymentDialog#launch") to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.62241587488E9)
                            )
                        )
                    ),
                    interactions = EngagementRecords(
                        records = mutableMapOf(
                            "55c94046a71b52ea570054d9" to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.62241587431E9)
                            )
                        )
                    ),
                    versionHistory = VersionHistory(
                        items = listOf(
                            VersionHistoryItem(
                                timestamp = 1.622415846539E9,
                                versionCode = 100000,
                                versionName = "1.0.0"
                            )
                        )
                    )
                ),
                engagementManifest = EngagementManifest()
            )
        }
        if (path == "5.7.0") {
            return Conversation(
                localIdentifier = "fa06592f-a7ef-4ba1-bbb8-a21ea9bbd4ec",
                conversationToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJ0eXBlIjoiYW5vbiIsImlzcyI6ImFwcHRlbnRpdmUiLCJzdWIiOiI2MTQ4ZmU5YjExMGQ1NTZmZWUwYmI3OGIiLCJhcHBfaWQiOiI1MjFmOWJiYTY4ZTI3NThiNmQwMDA5OGQiLCJpYXQiOjE2MzIxNzM3MjN9._I4runCaGrk6aoeIXX0LJJleN3IBjP8of3-FQACKWK9lFg0y9ItX5iMFE5bmijTl6pTb5fAAI1JuznFO5lHTTw",
                conversationId = "6148fe9b110d556fee0bb78b",
                device = Device(
                    osName = "Android",
                    osVersion = "9",
                    osBuild = "5875966",
                    osApiLevel = 28,
                    manufacturer = "Google",
                    model = "AOSP on IA Emulator",
                    board = "goldfish_x86",
                    product = "sdk_gphone_x86_arm",
                    brand = "google",
                    cpu = "x86",
                    device = "generic_x86_arm",
                    uuid = "7c24f320b2c58d43",
                    buildType = "user",
                    buildId = "PSR1.180720.117",
                    carrier = "T-Mobile",
                    currentCarrier = "Android",
                    networkType = "LTE",
                    bootloaderVersion = "unknown",
                    radioVersion = "1.0.0.0",
                    localeCountryCode = "US",
                    localeLanguageCode = "en",
                    localeRaw = "en_US",
                    utcOffset = -28800,
                    customData = CustomData(
                        content = mapOf(
                            "device-bool" to false,
                            "device-str" to "device",
                            "device-int" to 20
                        )
                    )
                ),
                person = Person(
                    id = "6148fe9b110d556fee0bb78a",
                    email = "person@company.com",
                    name = "First Second",
                    customData = CustomData(
                        content = mapOf(
                            "person-int" to 10,
                            "person-bool" to true,
                            "person-str" to "person"
                        )
                    )
                ),
                sdk = SDK(
                    version = "5.7.0",
                    platform = "Android",
                    distribution = "default",
                    distributionVersion = "5.7.0"
                ),
                appRelease = AppRelease(
                    type = "android",
                    identifier = "apptentive.com.android.feedback.test",
                    versionCode = 100000,
                    versionName = "1.0.0",
                    targetSdkVersion = "30",
                    minSdkVersion = "0",
                    debug = true,
                    inheritStyle = false,
                    overrideStyle = false,
                    appStore = null
                ),
                engagementData = EngagementData(
                    events = EngagementRecords(
                        records = mutableMapOf(
                            Event.parse("com.apptentive#app#launch") to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.632173722415E9)
                            ),
                            Event.parse("local#app#love_dialog_test") to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.632173724221E9)
                            ),
                            Event.parse("com.apptentive#EnjoymentDialog#launch") to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.63217372461E9)
                            ),
                            Event.parse("com.apptentive#TextModal#launch") to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.632173726464E9)
                            ),
                            Event.parse("com.apptentive#TextModal#dismiss") to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.63217372768E9)
                            ),
                            Event.parse("com.apptentive#EnjoymentDialog#yes") to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.63217372624E9)
                            )
                        )
                    ),
                    interactions = EngagementRecords(
                        records = mutableMapOf(
                            "58dbf79ef5dc8e6a2a000002" to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.632173726276E9)
                            ),
                            "55c94046a71b52ea570054d9" to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.632173724368E9)
                            )
                        )
                    ),
                    versionHistory = VersionHistory(
                        items = listOf(
                            VersionHistoryItem(
                                timestamp = 1.632173722926E9,
                                versionCode = 100000,
                                versionName = "1.0.0"
                            )
                        )
                    )
                ),
                engagementManifest = EngagementManifest()
            )
        }
        if (path == "5.7.0-login") {
            return Conversation(
                localIdentifier = "cdbe5242-9766-42f5-852d-a4fc3e2e6bac",
                conversationToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJ0eXBlIjoidXNlciIsInN1YiI6IlVzZXIiLCJpc3MiOiJDbGllbnRUZWFtIiwiaWF0IjoxNjMyMTgxODAxLCJleHAiOjE2MzQ3NzM4MDF9.1LRYjRUzNdic3UbHYCct05ezA_hXmIQsOnd1jdBLlqJ-Xen7CcA-W46hGHaxXAwoiXI7qmiaD-sr5u8XcjfI7Q",
                conversationId = "61491e21a9304e6e5609260e",
                device = Device(
                    osName = "Android",
                    osVersion = "9",
                    osBuild = "5875966",
                    osApiLevel = 28,
                    manufacturer = "Google",
                    model = "AOSP on IA Emulator",
                    board = "goldfish_x86",
                    product = "sdk_gphone_x86_arm",
                    brand = "google",
                    cpu = "x86",
                    device = "generic_x86_arm",
                    uuid = "7c24f320b2c58d43",
                    buildType = "user",
                    buildId = "PSR1.180720.117",
                    carrier = "T-Mobile",
                    currentCarrier = "Android",
                    networkType = "LTE",
                    bootloaderVersion = "unknown",
                    radioVersion = "1.0.0.0",
                    localeCountryCode = "US",
                    localeLanguageCode = "en",
                    localeRaw = "en_US",
                    utcOffset = -28800,
                    customData = CustomData(
                        content = mapOf(
                            "device-bool" to false,
                            "device-str" to "device",
                            "device-int" to 20
                        )
                    )
                ),
                person = Person(
                    id = "61491e21a9304e6e5609260d",
                    email = "person@company.com",
                    name = "First Second",
                    customData = CustomData(
                        content = mapOf(
                            "person-int" to 10,
                            "person-bool" to true,
                            "person-str" to "person"
                        )
                    )
                ),
                sdk = SDK(
                    version = "5.7.0",
                    platform = "Android",
                    distribution = "default",
                    distributionVersion = "5.7.0"
                ),
                appRelease = AppRelease(
                    type = "android",
                    identifier = "apptentive.com.android.feedback.test",
                    versionCode = 100000,
                    versionName = "1.0.0",
                    targetSdkVersion = "30",
                    minSdkVersion = "0",
                    debug = true,
                    inheritStyle = false,
                    overrideStyle = false,
                    appStore = null
                ),
                engagementData = EngagementData(
                    events = EngagementRecords(
                        records = mutableMapOf(
                            Event.parse("com.apptentive#app#launch") to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.632181792694E9)
                            ),
                            Event.parse("local#app#love_dialog_test") to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.632181795371E9)
                            ),
                            Event.parse("com.apptentive#EnjoymentDialog#launch") to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.632181795792E9)
                            ),
                            Event.parse("com.apptentive#TextModal#launch") to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.632181797799E9)
                            ),
                            Event.parse("com.apptentive#TextModal#dismiss") to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.632181799377E9)
                            ),
                            Event.parse("com.apptentive#EnjoymentDialog#yes") to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.632181797363E9)
                            ),
                            Event.parse("com.apptentive#app#login") to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.632181802536E9)
                            )
                        )
                    ),
                    interactions = EngagementRecords(
                        records = mutableMapOf(
                            "58dbf79ef5dc8e6a2a000002" to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.632181797416E9)
                            ),
                            "55c94046a71b52ea570054d9" to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.632181795508E9)
                            )
                        )
                    ),
                    versionHistory = VersionHistory(
                        items = listOf(
                            VersionHistoryItem(
                                timestamp = 1.632181793036E9,
                                versionCode = 100000,
                                versionName = "1.0.0"
                            )
                        )
                    )
                ),
                engagementManifest = EngagementManifest()
            )
        }
        if (path == "5.7.1") {
            return Conversation(
                localIdentifier = "d73d5ad1-6821-490a-909f-56c9e839bc4c",
                conversationToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJ0eXBlIjoiYW5vbiIsImlzcyI6ImFwcHRlbnRpdmUiLCJzdWIiOiI2MTQ5NjlhMTg3NTUxMDZlMzIxMzA2NGQiLCJhcHBfaWQiOiI1MjFmOWJiYTY4ZTI3NThiNmQwMDA5OGQiLCJpYXQiOjE2MzIyMDExMjF9.rI2esBJJSp39pXZzDQIsmO3s6ONeL-ifLypWGuAzL7LNhXXhUrozHIhfpx6jgSeZCbNarTn-z19326xGxvZG8A",
                conversationId = "614969a18755106e3213064d",
                device = Device(
                    osName = "Android",
                    osVersion = "9",
                    osBuild = "5875966",
                    osApiLevel = 28,
                    manufacturer = "Google",
                    model = "AOSP on IA Emulator",
                    board = "goldfish_x86",
                    product = "sdk_gphone_x86_arm",
                    brand = "google",
                    cpu = "x86",
                    device = "generic_x86_arm",
                    uuid = "7c24f320b2c58d43",
                    buildType = "user",
                    buildId = "PSR1.180720.117",
                    carrier = "T-Mobile",
                    currentCarrier = "Android",
                    networkType = "LTE",
                    bootloaderVersion = "unknown",
                    radioVersion = "1.0.0.0",
                    localeCountryCode = "US",
                    localeLanguageCode = "en",
                    localeRaw = "en_US",
                    utcOffset = -28800,
                    customData = CustomData(
                        content = mapOf(
                            "device-bool" to false,
                            "device-str" to "device",
                            "device-int" to 20
                        )
                    )
                ),
                person = Person(
                    id = "614969a18755106e3213064c",
                    email = "person@company.com",
                    name = "First Second",
                    customData = CustomData(
                        content = mapOf(
                            "person-int" to 10,
                            "person-bool" to true,
                            "person-str" to "person"
                        )
                    )
                ),
                sdk = SDK(
                    version = "5.7.1",
                    platform = "Android",
                    distribution = "default",
                    distributionVersion = "5.7.1"
                ),
                appRelease = AppRelease(
                    type = "android",
                    identifier = "apptentive.com.android.feedback.test",
                    versionCode = 100000,
                    versionName = "1.0.0",
                    targetSdkVersion = "30",
                    minSdkVersion = "0",
                    debug = true,
                    inheritStyle = false,
                    overrideStyle = false,
                    appStore = null
                ),
                engagementData = EngagementData(
                    events = EngagementRecords(
                        records = mutableMapOf(
                            Event.parse("com.apptentive#app#launch") to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.632201121219E9)
                            ),
                            Event.parse("local#app#love_dialog_test") to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.632201128356E9)
                            ),
                            Event.parse("com.apptentive#EnjoymentDialog#launch") to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.63220112888E9)
                            ),
                            Event.parse("com.apptentive#TextModal#launch") to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.632201130725E9)
                            ),
                            Event.parse("com.apptentive#TextModal#dismiss") to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.632201132084E9)
                            ),
                            Event.parse("com.apptentive#EnjoymentDialog#yes") to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.632201130445E9)
                            )
                        )
                    ),
                    interactions = EngagementRecords(
                        records = mutableMapOf(
                            "58dbf79ef5dc8e6a2a000002" to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.632201130484E9)
                            ),
                            "55c94046a71b52ea570054d9" to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.632201128491E9)
                            )
                        )
                    ),
                    versionHistory = VersionHistory(
                        items = listOf(
                            VersionHistoryItem(
                                timestamp = 1.632201121761E9,
                                versionCode = 100000,
                                versionName = "1.0.0"
                            )
                        )
                    )
                ),
                engagementManifest = EngagementManifest()
            )
        }
        if (path == "5.7.1-login") {
            return Conversation(
                localIdentifier = "f416460e-eec8-4fcf-9a9a-069f765e173d",
                conversationToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJ0eXBlIjoidXNlciIsInN1YiI6IlVzZXIiLCJpc3MiOiJDbGllbnRUZWFtIiwiaWF0IjoxNjMyMjAyNzcxLCJleHAiOjE2MzQ3OTQ3NzF9.pDagaC2OgzU3CuZUXgKcMjcX1VuMUmgPrksBDhRXGh1RNIM5fqUGo83d52pucPqaVTtJv_sd3qZL0rAYfWURwA",
                conversationId = "61497000745b226e2a14c379",
                device = Device(
                    osName = "Android",
                    osVersion = "9",
                    osBuild = "5875966",
                    osApiLevel = 28,
                    manufacturer = "Google",
                    model = "AOSP on IA Emulator",
                    board = "goldfish_x86",
                    product = "sdk_gphone_x86_arm",
                    brand = "google",
                    cpu = "x86",
                    device = "generic_x86_arm",
                    uuid = "7c24f320b2c58d43",
                    buildType = "user",
                    buildId = "PSR1.180720.117",
                    carrier = "T-Mobile",
                    currentCarrier = "Android",
                    networkType = "LTE",
                    bootloaderVersion = "unknown",
                    radioVersion = "1.0.0.0",
                    localeCountryCode = "US",
                    localeLanguageCode = "en",
                    localeRaw = "en_US",
                    utcOffset = -28800,
                    customData = CustomData(
                        content = mapOf(
                            "device-bool" to false,
                            "device-str" to "device",
                            "device-int" to 20
                        )
                    )
                ),
                person = Person(
                    id = "61497000745b226e2a14c378",
                    email = "person@company.com",
                    name = "First Second",
                    customData = CustomData(
                        content = mapOf(
                            "person-int" to 10,
                            "person-bool" to true,
                            "person-str" to "person"
                        )
                    )
                ),
                sdk = SDK(
                    version = "5.7.1",
                    platform = "Android",
                    distribution = "default",
                    distributionVersion = "5.7.1"
                ),
                appRelease = AppRelease(
                    type = "android",
                    identifier = "apptentive.com.android.feedback.test",
                    versionCode = 100000,
                    versionName = "1.0.0",
                    targetSdkVersion = "30",
                    minSdkVersion = "0",
                    debug = true,
                    inheritStyle = false,
                    overrideStyle = false,
                    appStore = null
                ),
                engagementData = EngagementData(
                    events = EngagementRecords(
                        records = mutableMapOf(
                            Event.parse("com.apptentive#app#launch") to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.632202751479E9)
                            ),
                            Event.parse("local#app#love_dialog_test") to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.632202758173E9)
                            ),
                            Event.parse("com.apptentive#EnjoymentDialog#launch") to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.632202758608E9)
                            ),
                            Event.parse("com.apptentive#TextModal#launch") to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.632202761195E9)
                            ),
                            Event.parse("com.apptentive#TextModal#dismiss") to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.632202763175E9)
                            ),
                            Event.parse("com.apptentive#EnjoymentDialog#yes") to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.632202760989E9)
                            ),
                            Event.parse("com.apptentive#app#login") to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.632202772377E9)
                            )
                        )
                    ),
                    interactions = EngagementRecords(
                        records = mutableMapOf(
                            "58dbf79ef5dc8e6a2a000002" to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.63220276103E9)
                            ),
                            "55c94046a71b52ea570054d9" to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.632202758361E9)
                            )
                        )
                    ),
                    versionHistory = VersionHistory(
                        items = listOf(
                            VersionHistoryItem(
                                timestamp = 1.632202751889E9,
                                versionCode = 100000,
                                versionName = "1.0.0"
                            )
                        )
                    )
                ),
                engagementManifest = EngagementManifest()
            )
        }
        if (path == "5.8.4") {
            return Conversation(
                localIdentifier = "b3c94e81-8b7a-4513-abf8-68c95cc71d2b",
                conversationToken = "eyJhbGciOiJIUzUxMiJ9.eyJ0eXBlIjoiYW5vbiIsImlzcyI6ImFwcHRlbnRpdmUiLCJzdWIiOiI2M2EyMGI1ZDFiZjYzZTFkZDAwMDFhMTUiLCJhcHBfaWQiOiI1MjFmOWJiYTY4ZTI3NThiNmQwMDA5OGQiLCJpYXQiOjE2NzE1NjQxMjV9.z-qkd-A6wOyza5VOPI8gSKmzJvMEI4Fz0RWIz7-0vtujx2hBw272YzKyBNytQopNur4Ijk4gZkup5hdMoZhRPg",
                conversationId = "63a20b5d1bf63e1dd0001a15",
                device = Device(
                    osName = "Android",
                    osVersion = "12",
                    osBuild = "8789670",
                    osApiLevel = 31,
                    manufacturer = "Google",
                    model = "sdk_gphone64_x86_64",
                    board = "goldfish_x86_64",
                    product = "sdk_gphone64_x86_64",
                    brand = "google",
                    cpu = "x86_64",
                    device = "emulator64_x86_64_arm64",
                    uuid = "85f6812e-ca6c-4ac7-91fd-8e40c790d369",
                    buildType = "userdebug",
                    buildId = "SE1A.220630.001",
                    carrier = "T-Mobile",
                    currentCarrier = "T-Mobile",
                    bootloaderVersion = "unknown",
                    radioVersion = "1.0.0.0",
                    localeCountryCode = "US",
                    localeLanguageCode = "en",
                    localeRaw = "en_US",
                    utcOffset = -28800,
                    customData = CustomData(
                        content = mapOf(
                            "device-bool" to false,
                            "device-str" to "device",
                            "device-int" to 20
                        )
                    )
                ),
                person = Person(
                    id = "63a20b5d1bf63e1dd0001a13",
                    email = "person@company.com",
                    name = "First Second",
                    customData = CustomData(
                        content = mapOf(
                            "person-int" to 10,
                            "person-bool" to true,
                            "person-str" to "person"
                        )
                    )
                ),
                sdk = SDK(
                    version = "5.8.4",
                    platform = "Android",
                    distribution = "default",
                    distributionVersion = "5.8.4"
                ),
                appRelease = AppRelease(
                    type = "android",
                    identifier = "apptentive.com.android.feedback.test",
                    versionCode = 100000,
                    versionName = "1.0.0",
                    targetSdkVersion = "31",
                    minSdkVersion = "0",
                    debug = true,
                    inheritStyle = false,
                    overrideStyle = false,
                    appStore = null
                ),
                engagementData = EngagementData(
                    events = EngagementRecords(
                        records = mutableMapOf(
                            Event.parse("com.apptentive#app#launch") to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.671564082932E9)
                            ),
                            Event.parse("local#app#love_dialog_test") to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.671564175562E9)
                            ),
                            Event.parse("com.apptentive#EnjoymentDialog#launch") to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.671564176032E9)
                            ),
                            Event.parse("com.apptentive#TextModal#launch") to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.671564179587E9)
                            ),
                            Event.parse("com.apptentive#TextModal#dismiss") to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.671564181341E9)
                            ),
                            Event.parse("com.apptentive#EnjoymentDialog#yes") to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.671564179289E9)
                            )
                        )
                    ),
                    interactions = EngagementRecords(
                        records = mutableMapOf(
                            "58dbf79ef5dc8e6a2a000002" to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.671564179408E9)
                            ),
                            "55c94046a71b52ea570054d9" to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.671564175739E9)
                            )
                        )
                    ),
                    versionHistory = VersionHistory(
                        items = listOf(
                            VersionHistoryItem(
                                timestamp = 1.671564082994E9,
                                versionCode = 100000,
                                versionName = "1.0.0"
                            )
                        )
                    )
                ),
                engagementManifest = EngagementManifest()
            )
        }
        if (path == "5.8.4-login") {
            return Conversation(
                localIdentifier = "b3c94e81-8b7a-4513-abf8-68c95cc71d2b",
                conversationToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJ0eXBlIjoidXNlciIsInN1YiI6IlVzZXIiLCJpc3MiOiJDbGllbnRUZWFtIiwiaWF0IjoxNjcxNTY1MTA2LCJleHAiOjE2NzQxNTcxMDZ9.FN2M9hD9idyfXq7cUz1oPDxiSmsBimmruUPDevtvh7NYAnepKCrWlHy6qbGdj8T-n46bLUUmfR-qT7mDX8Wj4w",
                conversationId = "63a20b5d1bf63e1dd0001a15",
                device = Device(
                    osName = "Android",
                    osVersion = "12",
                    osBuild = "8789670",
                    osApiLevel = 31,
                    manufacturer = "Google",
                    model = "sdk_gphone64_x86_64",
                    board = "goldfish_x86_64",
                    product = "sdk_gphone64_x86_64",
                    brand = "google",
                    cpu = "x86_64",
                    device = "emulator64_x86_64_arm64",
                    uuid = "85f6812e-ca6c-4ac7-91fd-8e40c790d369",
                    buildType = "userdebug",
                    buildId = "SE1A.220630.001",
                    carrier = "T-Mobile",
                    currentCarrier = "T-Mobile",
                    bootloaderVersion = "unknown",
                    radioVersion = "1.0.0.0",
                    localeCountryCode = "US",
                    localeLanguageCode = "en",
                    localeRaw = "en_US",
                    utcOffset = -28800,
                    customData = CustomData(
                        content = mapOf(
                            "device-bool" to false,
                            "device-str" to "device",
                            "device-int" to 20
                        )
                    )
                ),
                person = Person(
                    id = "63a20b5d1bf63e1dd0001a13",
                    email = "person@company.com",
                    name = "First Second",
                    customData = CustomData(
                        content = mapOf(
                            "person-int" to 10,
                            "person-bool" to true,
                            "person-str" to "person"
                        )
                    )
                ),
                sdk = SDK(
                    version = "5.8.4",
                    platform = "Android",
                    distribution = "default",
                    distributionVersion = "5.8.4"
                ),
                appRelease = AppRelease(
                    type = "android",
                    identifier = "apptentive.com.android.feedback.test",
                    versionCode = 100000,
                    versionName = "1.0.0",
                    targetSdkVersion = "31",
                    minSdkVersion = "0",
                    debug = true,
                    inheritStyle = false,
                    overrideStyle = false,
                    appStore = null
                ),
                engagementData = EngagementData(
                    events = EngagementRecords(
                        records = mutableMapOf(
                            Event.parse("com.apptentive#app#launch") to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.671564082932E9)
                            ),
                            Event.parse("local#app#love_dialog_test") to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.671564175562E9)
                            ),
                            Event.parse("com.apptentive#EnjoymentDialog#launch") to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.671564176032E9)
                            ),
                            Event.parse("com.apptentive#TextModal#launch") to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.671564179587E9)
                            ),
                            Event.parse("com.apptentive#TextModal#dismiss") to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.671564181341E9)
                            ),
                            Event.parse("com.apptentive#EnjoymentDialog#yes") to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.671564179289E9)
                            ),
                            Event.parse("com.apptentive#app#login") to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.671565107845E9)
                            ),
                        )
                    ),
                    interactions = EngagementRecords(
                        records = mutableMapOf(
                            "58dbf79ef5dc8e6a2a000002" to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.671564179408E9)
                            ),
                            "55c94046a71b52ea570054d9" to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.671564175739E9)
                            )
                        )
                    ),
                    versionHistory = VersionHistory(
                        items = listOf(
                            VersionHistoryItem(
                                timestamp = 1.671564082994E9,
                                versionCode = 100000,
                                versionName = "1.0.0"
                            )
                        )
                    )
                ),
                engagementManifest = EngagementManifest()
            )
        } else TODO("Implement me")
    }

    private fun pushFiles(sdkVersion: String) {
        copyAsset(sdkVersion, context.dataDir)
    }

    private fun copyAsset(path: String, dstDir: File) {
        val list = context.assets.list(path)
        if (list != null && list.isNotEmpty()) {
            list.forEach { copyAsset("$path/$it", dstDir) }
        } else {
            val dst = File(dstDir, path.removePrefixPathComponent())
            val parentDir = dst.parentFile
            if (!parentDir.exists()) {
                parentDir.mkdirs()
            }
            print("Copying $path -> $dst")

            context.assets.open(path).use { input ->
                dst.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
    }
}

private fun String.removePrefixPathComponent(): String {
    val tokens = split("/")
    return tokens.subList(1, tokens.size).joinToString("/")
}
