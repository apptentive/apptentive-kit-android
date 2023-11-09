package apptentive.com.android.feedback.payload

import android.os.Build
import androidx.annotation.RequiresApi
import apptentive.com.android.encryption.AESEncryption23
import apptentive.com.android.encryption.EncryptionKey
import apptentive.com.android.feedback.model.payloads.EncryptedPayloadPart
import apptentive.com.android.feedback.model.payloads.Payload
import apptentive.com.android.feedback.model.payloads.PayloadPart
import apptentive.com.android.feedback.utils.MultipartParser
import apptentive.com.android.util.InternalUseOnly
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags
import java.io.ByteArrayInputStream

@InternalUseOnly
class EncryptedPayloadTokenUpdater {
    companion object {
        @RequiresApi(Build.VERSION_CODES.M)
        fun updateEmbeddedToken(
            token: String,
            encryptionKey: EncryptionKey,
            payloadType: PayloadType,
            contentType: MediaType?,
            data: ByteArray
        ): ByteArray {
            if ((contentType?.type == "application") && (contentType.subType == "octet-stream")) {
                return encrypt(
                    updateJSON(token, decrypt(data, encryptionKey)),
                    encryptionKey
                )
            } else if (contentType?.type == "multipart" && contentType.subType == "encrypted") {
                // Attempt to get the multi-part boundary from the Content-Type header.
                val boundary = contentType?.parameters?.get("boundary") ?: Payload.BOUNDARY
                val inputStream = ByteArrayInputStream(data)

                val parser = MultipartParser(inputStream, boundary)
                if (parser.numberOfParts > 0) {
                    // Split the first part into header and content pieces.
                    val firstPart = parser.getPartAtIndex(0)

                    // The first part is the JSON that contains the token we need to update.
                    if (firstPart != null) {
                        // Decrypt the non-header portion of the part.
                        val decryptedPartData = decrypt(firstPart.content, encryptionKey)

                        // Also split the decrypted part into headers and content.
                        val decryptedPart = MultipartParser.parsePart(ByteArrayInputStream(decryptedPartData), 0L..decryptedPartData.size + 2)

                        if (decryptedPart != null) {
                            // Update the JSON with the new token
                            val updatedJson = updateJSON(token, decryptedPart.content)

                            // Create a new PayloadPart-implementing instance with the updated JSON.
                            val updatedPart = MultipartParser.Part(
                                decryptedPart.multipartHeaders,
                                updatedJson,
                                MediaType.applicationJson,
                                payloadType.jsonContainer()
                            )

                            // Create the list of parts for the updated payload data.
                            val parts = mutableListOf<PayloadPart>(
                                EncryptedPayloadPart(updatedPart, encryptionKey, true)
                            )

                            // Add any remaining parts to the list as-is.
                            if (parser.numberOfParts > 1) {
                                for (i in 1 until parser.numberOfParts) {
                                    parser.getPartAtIndex(i)?.let { parts.add(it) }
                                }
                            }

                            // Assemble the parts into payload data.
                            return Payload.assembleMultipart(parts, boundary)
                        }
                    }
                }

                Log.w(LogTags.PAYLOADS, "Unrecognized multipart format for payload.")
                return data
            } else {
                Log.w(LogTags.PAYLOADS, "Unrecognized content type for updating embedded token.")
                return data
            }
        }

        internal fun updateJSON(
            token: String,
            data: ByteArray
        ): ByteArray {
            val jsonString = String(data, Charsets.UTF_8)
            // Have to do a regex replace because the JSON codec mangles the timestamp number format
            val regex = "\"token\":\"[^\"]+\""
            val updatedJsonString = jsonString.replaceFirst(Regex(regex), "\"token\":\"$token\"")
            return updatedJsonString.toByteArray()
        }

        @RequiresApi(Build.VERSION_CODES.M)
        internal fun decrypt(data: ByteArray, encryptionKey: EncryptionKey): ByteArray {
            return AESEncryption23(encryptionKey).decryptPayloadData(data)
        }

        @RequiresApi(Build.VERSION_CODES.M)
        internal fun encrypt(data: ByteArray, encryptionKey: EncryptionKey): ByteArray {
            return AESEncryption23(encryptionKey).encryptPayloadData(data)
        }
    }
}
