package apptentive.com.android.feedback.model

import apptentive.com.android.debug.Assert.assertEqual
import apptentive.com.android.feedback.utils.DataBinaryNullableInput
import apptentive.com.android.feedback.utils.DataBinaryNullableOutput
import kotlinx.serialization.decode
import kotlinx.serialization.encode
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
                buildId = "buildId"
            ).apply {
                carrier = "carrier"
                currentCarrier = "currentCarrier"
                networkType = "networkType"
                bootloaderVersion = "bootloaderVersion"
                radioVersion = "radioVersion"
                localeCountryCode = "localeCountryCode"
                localeLanguageCode = "localeLanguageCode"
                localeRaw = "localeRaw"
                utcOffset = "utcOffset"
                advertiserId = "advertiserId"
            }
        )

        val serializer = ConversationData.serializer()

        val baos = ByteArrayOutputStream()
        val out = DataBinaryNullableOutput(DataOutputStream(baos))
        out.encode(serializer, actual)

        val bais = ByteArrayInputStream(baos.toByteArray())
        val input = DataBinaryNullableInput(DataInputStream(bais))
        val expected = input.decode(serializer)

        assertEqual(actual, expected)
    }
}