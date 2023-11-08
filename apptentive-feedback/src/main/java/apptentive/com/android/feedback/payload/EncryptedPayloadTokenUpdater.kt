package apptentive.com.android.feedback.payload

import android.os.Build
import androidx.annotation.RequiresApi
import apptentive.com.android.encryption.AESEncryption23
import apptentive.com.android.encryption.EncryptionKey
import apptentive.com.android.feedback.model.payloads.EncryptedPayloadPart
import apptentive.com.android.feedback.model.payloads.Payload
import apptentive.com.android.feedback.model.payloads.PayloadPart
import apptentive.com.android.feedback.utils.MultipartParser
import apptentive.com.android.serialization.json.JsonConverter
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
                    updateJSON(token, payloadType, decrypt(data, encryptionKey)),
                    encryptionKey
                )
            } else if (contentType?.type == "multipart" && contentType.subType == "encrypted") {
                val boundary = contentType?.parameters?.get("boundary") ?: "s16u0iwtqlokf4v9cpgne8a2amdrxz735hjby"
                val inputStream = ByteArrayInputStream(data)
                val parser = MultipartParser(inputStream, boundary)
                if (parser.numberOfParts > 0) {
                    val firstPart = parser.getPartAtIndex(0)
                    if (firstPart != null) {
                        val decryptedPartData = decrypt(firstPart.content, encryptionKey)
                        val decryptedPart = MultipartParser.parsePart(ByteArrayInputStream(decryptedPartData), 0L..decryptedPartData.size + 2)
                        if (decryptedPart != null) {
                            val updatedJson = updateJSON(token, payloadType, decryptedPart.content)
                            val updatedPart = MultipartParser.Part(
                                decryptedPart.multipartHeaders,
                                updatedJson,
                                MediaType.applicationJson,
                                payloadType.asString()
                            )

                            val parts = mutableListOf<PayloadPart>(
                                EncryptedPayloadPart(updatedPart, encryptionKey, true)
                            )

                            if (parser.numberOfParts > 1) {
                                for (i in 1 until parser.numberOfParts) {
                                    parser.getPartAtIndex(i)?.let { parts.add(it) }
                                }
                            }

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
            payloadType: PayloadType,
            data: ByteArray
        ): ByteArray {
            val json = JsonConverter.toMap(String(data, Charsets.UTF_8)).toMutableMap()

            if (payloadType == PayloadType.Message || payloadType == PayloadType.Logout) {
                // No outer object (see below).
                json["token"] = token
            } else {
                // For most payloads, there's an outer object with a key corresponding to the
                // payload type and the value set to the object that contains the token/data.
                val jsonContainer = payloadType.asString()
                val nestedJson = json[jsonContainer] as? MutableMap<String, Any>
                nestedJson?.put("token", token)
                json[jsonContainer] = nestedJson
            }

            return JsonConverter.toJson(json).toByteArray()
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
