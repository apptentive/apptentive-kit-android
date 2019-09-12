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
    fun serialization() {
        val data = ConversationData(
            "localIdentifier",
            "conversationToken",
            "conversationId"
        )

        val serializer = ConversationData.serializer()

        val baos = ByteArrayOutputStream()
        val out = DataBinaryNullableOutput(DataOutputStream(baos))
        out.encode(serializer, data)

        val bais = ByteArrayInputStream(baos.toByteArray())
        val input = DataBinaryNullableInput(DataInputStream(bais))
        val data1 = input.decode(serializer)

        assertEqual(data, data1)
    }
}