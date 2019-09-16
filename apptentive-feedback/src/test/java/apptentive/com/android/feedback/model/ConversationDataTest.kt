package apptentive.com.android.feedback.model

import apptentive.com.android.debug.Assert
import apptentive.com.android.serialization.BinaryDecoder
import apptentive.com.android.serialization.BinaryEncoder
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream

class ConversationDataTest {
    @Test
    fun binarySerialization() {
        val actual = ConversationData(
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
                utcOffset = "utcOffset",
                advertiserId = "advertiserId",
                customData = CustomData(mapOf(Pair("key", "value"))),
                integrationConfig = IntegrationConfig(
                    apptentive = IntegrationConfigItem(mapOf(Pair("key1", "value1"))),
                    amazonAwsSns = IntegrationConfigItem(mapOf(Pair("key2", "value2"))),
                    urbanAirship = IntegrationConfigItem(mapOf(Pair("key3", "value3"))),
                    parse = IntegrationConfigItem(mapOf(Pair("key4", "value4")))
                )
            ),
            person = Person()
        )

        val baos = ByteArrayOutputStream()
        val out = BinaryEncoder(DataOutputStream(baos))
        out.encode(actual)

        val bais = ByteArrayInputStream(baos.toByteArray())
        val input = BinaryDecoder(DataInputStream(bais))
        val expected = input.decodeConversationData()

        Assert.assertEqual(actual, expected)
    }
}