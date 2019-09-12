package apptentive.com.android.feedback.utils

import kotlinx.serialization.CompositeEncoder
import kotlinx.serialization.ElementValueEncoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.internal.EnumDescriptor
import java.io.DataOutput

class DataBinaryNullableOutput(val out: DataOutput) : ElementValueEncoder() {
    override fun beginCollection(
        desc: SerialDescriptor,
        collectionSize: Int,
        vararg typeParams: KSerializer<*>
    ): CompositeEncoder {
        return super.beginCollection(desc, collectionSize, *typeParams).also {
            out.writeInt(collectionSize)
        }
    }
    override fun encodeNull() = out.writeByte(0)
    override fun encodeNotNullMark() = out.writeByte(1)
    override fun encodeBoolean(value: Boolean) = out.writeByte(if (value) 1 else 0)
    override fun encodeByte(value: Byte) = out.writeByte(value.toInt())
    override fun encodeShort(value: Short) = out.writeShort(value.toInt())
    override fun encodeInt(value: Int) = out.writeInt(value)
    override fun encodeLong(value: Long) = out.writeLong(value)
    override fun encodeFloat(value: Float) = out.writeFloat(value)
    override fun encodeDouble(value: Double) = out.writeDouble(value)
    override fun encodeChar(value: Char) = out.writeChar(value.toInt())
    override fun encodeString(value: String) = out.writeUTF(value)
    override fun encodeEnum(enumDescription: EnumDescriptor, ordinal: Int) = out.writeInt(ordinal)
}