package apptentive.com.android.feedback.model.payloads

import android.os.Build
import androidx.annotation.RequiresApi
import apptentive.com.android.encryption.AESEncryption23
import apptentive.com.android.encryption.EncryptionKey
import java.io.ByteArrayOutputStream

internal class EncryptedPayloadPart(private val payloadPart: PayloadPart, val encryptionKey: EncryptionKey, private val includeHeaders: Boolean) :
    PayloadPart {
    override val filename = payloadPart.filename
    override val parameterName = payloadPart.parameterName
    override val content: ByteArray @RequiresApi(Build.VERSION_CODES.M)
    get() {
        val plaintextData = ByteArrayOutputStream()
        val encryptedData = ByteArrayOutputStream()

        if (includeHeaders) { // Required when part of our multipart/encrypted encoding
            plaintextData.write(payloadPart.multipartHeaders.toByteArray())
            plaintextData.write(Payload.LINE_END.toByteArray())
            plaintextData.write(payloadPart.content)
            // plaintextData.write(Payload.LINE_END.toByteArray()) // TODO: Add this back once API is fixed.
        } else {
            plaintextData.write(payloadPart.content)
        }

        encryptedData.write(AESEncryption23(encryptionKey).encryptPayloadData(plaintextData.toByteArray()))

        return encryptedData.toByteArray()
    }
}
