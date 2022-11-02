package apptentive.com.android.feedback.message

import androidx.core.util.AtomicFile
import apptentive.com.android.serialization.BinaryDecoder
import apptentive.com.android.serialization.BinaryEncoder
import apptentive.com.android.serialization.Decoder
import apptentive.com.android.serialization.Encoder
import apptentive.com.android.serialization.TypeSerializer
import apptentive.com.android.serialization.decodeList
import apptentive.com.android.serialization.decodeNullableString
import apptentive.com.android.serialization.encodeList
import apptentive.com.android.serialization.encodeNullableString
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags
import apptentive.com.android.util.LogTags.MESSAGE_CENTER
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.EOFException
import java.io.File

internal interface MessageSerializer {
    @Throws(MessageSerializerException::class)
    fun loadMessages(): List<DefaultMessageRepository.MessageEntry>

    @Throws(MessageSerializerException::class)
    fun saveMessages(messages: List<DefaultMessageRepository.MessageEntry>)
}

internal class DefaultMessageSerializer(val messagesFile: File) : MessageSerializer {
    override fun loadMessages(): List<DefaultMessageRepository.MessageEntry> {
        return if (messagesFile.exists()) {
            Log.d(MESSAGE_CENTER, "Loading messages from MessagesFile")
            readMessageEntries()
        } else {
            Log.d(MESSAGE_CENTER, "MessagesFile doesn't exist")
            listOf()
        }
    }

    override fun saveMessages(messages: List<DefaultMessageRepository.MessageEntry>) {
        val start = System.currentTimeMillis()
        val atomicFile = AtomicFile(messagesFile)
        val stream = atomicFile.startWrite()
        try {
            val encoder = BinaryEncoder(DataOutputStream(stream))
            messageSerializer.encode(encoder, messages)
            atomicFile.finishWrite(stream)
        } catch (e: Exception) {
            atomicFile.failWrite(stream)
            throw MessageSerializerException("Unable to save messages", e)
        }

        Log.v(LogTags.CONVERSATION, "Messages saved (took ${System.currentTimeMillis() - start} ms)")
    }

    private fun readMessageEntries(): List<DefaultMessageRepository.MessageEntry> {
        try {
            return messagesFile.inputStream().use { stream ->
                val decoder = BinaryDecoder(DataInputStream(stream))
                messageSerializer.decode(decoder)
            }
        } catch (e: EOFException) {
            throw MessageSerializerException("Unable to load messages: file corrupted", e)
        }
    }

    private val messageSerializer: TypeSerializer<List<DefaultMessageRepository.MessageEntry>> by lazy {
        object : TypeSerializer<List<DefaultMessageRepository.MessageEntry>> {
            override fun encode(encoder: Encoder, value: List<DefaultMessageRepository.MessageEntry>) {
                encoder.encodeList(value) { item ->
                    encoder.encodeNullableString(item.id)
                    encoder.encodeDouble(item.createdAt)
                    encoder.encodeString(item.nonce)
                    encoder.encodeString(item.messageState)
                    encoder.encodeString(item.messageJson)
                }
            }

            override fun decode(decoder: Decoder): List<DefaultMessageRepository.MessageEntry> {
                return decoder.decodeList {
                    DefaultMessageRepository.MessageEntry(
                        id = decoder.decodeNullableString(),
                        createdAt = decoder.decodeDouble(),
                        nonce = decoder.decodeString(),
                        messageState = decoder.decodeString(),
                        messageJson = decoder.decodeString()
                    )
                }
            }
        }
    }
}
