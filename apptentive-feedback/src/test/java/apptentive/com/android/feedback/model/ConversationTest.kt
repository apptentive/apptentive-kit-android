package apptentive.com.android.feedback.model

import apptentive.com.android.TestCase
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.encryption.EncryptionFactory
import apptentive.com.android.encryption.NotEncrypted
import apptentive.com.android.feedback.conversation.DefaultConversationSerializer
import apptentive.com.android.feedback.engagement.util.MockAndroidSharedPrefDataStore
import apptentive.com.android.platform.AndroidSharedPrefDataStore
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import java.io.File

class ConversationTest : TestCase() {

    @Before
    override fun setUp() {
        DependencyProvider.register<AndroidSharedPrefDataStore>(MockAndroidSharedPrefDataStore())
    }
    @Test
    fun binaryFileSerialization() {
        val expected = Conversation(
            localIdentifier = "localIdentifier",
            conversationToken = "conversationToken",
            conversationId = "conversationId",
            device = Device(
                osName = "osName",
                osVersion = "osVersion",
                osBuild = "osBuild",
                osApiLevel = 10,
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
                utcOffset = -8,
                customData = CustomData(mapOf(Pair("key", "value"))),
                integrationConfig = IntegrationConfig(
                    apptentive = IntegrationConfigItem(mapOf(Pair("key1", "value1"))),
                    amazonAwsSns = IntegrationConfigItem(mapOf(Pair("key2", "value2"))),
                    urbanAirship = IntegrationConfigItem(mapOf(Pair("key3", "value3"))),
                    parse = IntegrationConfigItem(mapOf(Pair("key4", "value4")))
                )
            ),
            person = Person(
                id = "id",
                email = "email",
                name = "name",
                mParticleId = "mParticleId",
                customData = CustomData(mapOf(Pair("key5", "value5")))
            ),
            sdk = SDK(
                version = "version",
                platform = "platform",
                distribution = "distribution",
                distributionVersion = "distributionVersion",
                programmingLanguage = "programmingLanguage",
                authorName = "authorName",
                authorEmail = "authorEmail"
            ),
            appRelease = AppRelease(
                type = "type",
                identifier = "identifier",
                versionCode = 1,
                versionName = "versionName",
                targetSdkVersion = "targetSdkVersion",
                minSdkVersion = "minSdkVersion",
                debug = true,
                inheritStyle = false,
                overrideStyle = true,
                appStore = "appStore"
            ),
            engagementManifest = EngagementManifest(), // TODO: pass actual value
            engagementData = EngagementData()
        )

        val serializer = createSerializer()

        // should return no conversation before anything was saved
        assertThat(serializer.loadConversation()).isNull()

        // save conversation
        serializer.saveConversation(expected)

        val actual = serializer.loadConversation()
        assertThat(expected).isEqualTo(actual)
    }

    @Test
    fun binarySerializationMissingData() {
        val expected = Conversation(
            localIdentifier = "localIdentifier",
            device = Device(
                osName = "osName",
                osVersion = "osVersion",
                osBuild = "osBuild",
                osApiLevel = 10,
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
                localeCountryCode = "EN",
                localeLanguageCode = "en_us",
                localeRaw = "localeRaw",
                utcOffset = -8
            ),
            person = Person(),
            sdk = SDK(
                version = "version",
                platform = "platform",
                distribution = "distribution",
                distributionVersion = "distributionVersion"
            ),
            appRelease = AppRelease(
                type = "type",
                identifier = "identifier",
                versionCode = 1,
                versionName = "versionName",
                targetSdkVersion = "targetSdkVersion",
                minSdkVersion = "minSdkVersion"
            ),
            engagementManifest = EngagementManifest(), // TODO: pass actual value
            engagementData = EngagementData()
        )

        val serializer = createSerializer()

        // save conversation
        serializer.saveConversation(expected)

        val actual = serializer.loadConversation()
        assertThat(expected).isEqualTo(actual)
    }

    private fun createSerializer(): DefaultConversationSerializer {
        val conversationFile = createTempFile()
        val manifestFile = createTempFile()

        return DefaultConversationSerializer(
            conversationFile, manifestFile,
        ).apply {
            setEncryption(
                EncryptionFactory.getEncryption(
                    shouldEncryptStorage = false,
                    oldEncryptionSetting = NotEncrypted
                )
            )
        }
    }
}

private fun createTempFile(): File {
    val file = kotlin.io.createTempFile()
    file.delete()
    return file
}
