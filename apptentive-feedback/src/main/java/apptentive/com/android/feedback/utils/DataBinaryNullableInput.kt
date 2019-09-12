package apptentive.com.android.feedback.utils

import kotlinx.serialization.ElementValueDecoder
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.internal.EnumDescriptor
import java.io.DataInput

class DataBinaryNullableInput(val inp: DataInput) : ElementValueDecoder() {
    override fun decodeCollectionSize(desc: SerialDescriptor): Int = inp.readInt()
    override fun decodeNotNullMark(): Boolean = inp.readByte() != 0.toByte()
    override fun decodeBoolean(): Boolean = inp.readByte().toInt() != 0
    override fun decodeByte(): Byte = inp.readByte()
    override fun decodeShort(): Short = inp.readShort()
    override fun decodeInt(): Int = inp.readInt()
    override fun decodeLong(): Long = inp.readLong()
    override fun decodeFloat(): Float = inp.readFloat()
    override fun decodeDouble(): Double = inp.readDouble()
    override fun decodeChar(): Char = inp.readChar()
    override fun decodeString(): String = inp.readUTF()
    override fun decodeEnum(enumDescription: EnumDescriptor): Int = inp.readInt()
}