package apptentive.com.android.feedback.message

import androidx.core.util.AtomicFile
import apptentive.com.android.encryption.Encryption
import apptentive.com.android.feedback.conversation.ConversationRoster
import apptentive.com.android.feedback.utils.FileStorageUtils
import apptentive.com.android.feedback.utils.FileStorageUtils.getMessagesFile
import apptentive.com.android.feedback.utils.FileUtil
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
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.EOFException
import java.io.File
import java.io.FileInputStream

internal interface MessageSerializer {
    @Throws(MessageSerializerException::class)
    fun loadMessages(): List<DefaultMessageRepository.MessageEntry>

    @Throws(MessageSerializerException::class)
    fun saveMessages(messages: List<DefaultMessageRepository.MessageEntry>)

    fun deleteMessageFile(messageFile: File)

    fun updateEncryption(encryption: Encryption)

    fun updateConversionRoster(conversationRoster: ConversationRoster)
}

internal class DefaultMessageSerializer(var encryption: Encryption, var conversationRoster: ConversationRoster) : MessageSerializer {

    override fun loadMessages(): List<DefaultMessageRepository.MessageEntry> {
        val messagesFile = getMessageFileCreatedBeforeMultiUser() ?: getMessageFileFromRoster(conversationRoster)
        return if (messagesFile.exists()) {
            Log.d(MESSAGE_CENTER, "Loading messages from MessagesFile")
            val messageEntries = readMessageEntries(messagesFile)
            if (getMessageFileFromRoster(conversationRoster).length() == 0L) {
                switchMessageCachingThroughRoster(messageEntries)
            }
            messageEntries
        } else {
            Log.d(MESSAGE_CENTER, "MessagesFile doesn't exist")
            listOf()
        }
    }

    override fun saveMessages(messages: List<DefaultMessageRepository.MessageEntry>) {
        val messagesFile = getMessageFileFromRoster(conversationRoster)
        val start = System.currentTimeMillis()
        val atomicFile = AtomicFile(messagesFile)
        val stream = atomicFile.startWrite()
        val byteArrayOutputStream = ByteArrayOutputStream()
        try {
            val encoder = BinaryEncoder(DataOutputStream(byteArrayOutputStream))
            messageSerializer.encode(encoder, messages)
            val encryptedBytes = encryption.encrypt(byteArrayOutputStream.toByteArray())
            stream.use {
                stream.write(encryptedBytes)
                atomicFile.finishWrite(stream)
            }
        } catch (e: Exception) {
            atomicFile.failWrite(stream)
            throw MessageSerializerException("Unable to save messages", e)
        }

        Log.v(LogTags.CONVERSATION, "Messages saved (took ${System.currentTimeMillis() - start} ms)")
    }

    override fun deleteMessageFile(messageFile: File) {
        FileUtil.deleteFile(messageFile.path)
        Log.w(LogTags.CRYPTOGRAPHY, "Message cache is deleted to support the new encryption setting")
    }

    override fun updateEncryption(encryption: Encryption) {
        this.encryption = encryption
    }

    override fun updateConversionRoster(conversationRoster: ConversationRoster) {
        this.conversationRoster = conversationRoster
    }

    private fun switchMessageCachingThroughRoster(messageEntries: List<DefaultMessageRepository.MessageEntry>) {
        // Transfer the entries to new message file
        saveMessages(messageEntries)
        // Delete old messages.bin if it exists
        getMessageFileCreatedBeforeMultiUser()?.let { oldMessagesFile ->
            FileUtil.deleteFile(oldMessagesFile.path)
        }
    }

    private fun getMessageFileFromRoster(roster: ConversationRoster): File {
        Log.d(MESSAGE_CENTER, "Setting message file from roster: $roster")

        val activeConversationMetadata = roster.activeConversation
            ?: throw MessageSerializerException("Unable to load messages: no active conversation", Throwable())
        return FileStorageUtils.getMessagesFileForActiveUser(activeConversationMetadata.path)
    }

    private fun getMessageFileCreatedBeforeMultiUser(): File? {
        val messagesFile = getMessagesFile()
        return if (messagesFile.exists())
            messagesFile
        else null
    }

    private fun readMessageEntries(messagesFile: File): List<DefaultMessageRepository.MessageEntry> =
        try {
            val decryptedMessage = encryption.decrypt(FileInputStream(messagesFile))
            val inputStream = ByteArrayInputStream(decryptedMessage)
            val decoder = BinaryDecoder(DataInputStream(inputStream))
            messageSerializer.decode(decoder)
        } catch (e: EOFException) {
            throw MessageSerializerException("Unable to load messages: file corrupted", e)
        } catch (e: Exception) {
            throw MessageSerializerException("Unable to load conversation", e)
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
