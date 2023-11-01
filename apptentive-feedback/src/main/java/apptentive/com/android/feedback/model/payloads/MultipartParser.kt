package apptentive.com.android.feedback.model.payloads

import apptentive.com.android.feedback.utils.StreamSearcher
import java.io.InputStream

class MultipartParser(
    val inputStream: InputStream,
    private val boundary: String
) {
    val numberOfParts: Int get() = ranges.count()
    private var ranges = mutableListOf<LongRange>()

    init {
        ranges = getPartRanges() ?: mutableListOf()
    }

    fun getPartAtIndex(index: Int): Part? {
        if (index > numberOfParts) {
            return null
        }

        return MultipartParser.parsePart(inputStream, ranges[index])
    }

    data class Part (
        val headers: String,
        val content: ByteArray
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Part

            if (headers != other.headers) return false
            if (!content.contentEquals(other.content)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = headers.hashCode()
            result = 31 * result + content.contentHashCode()
            return result
        }
    }

    companion object {
        fun parsePart(inputStream: InputStream, range: LongRange): Part? {
            val partStart = range.first

            inputStream.reset()
            inputStream.skip(partStart)
            val endOfBlankLineIndex = getEndOfHeaders(inputStream)

            if (endOfBlankLineIndex != null) {
                inputStream.reset()
                inputStream.skip(partStart)
                val lengthOfHeaders = endOfBlankLineIndex.toInt() - 4
                var headerByteArray = ByteArray(lengthOfHeaders)

                inputStream.read(
                    headerByteArray,
                    0,
                    lengthOfHeaders
                )

                inputStream.skip(4)

                val lengthOfContent = range.count() - endOfBlankLineIndex.toInt() - 3
                val contentByteArray = ByteArray(lengthOfContent)
                inputStream.read(
                    contentByteArray,
                    0,
                    lengthOfContent)

                return Part(String(headerByteArray, Charsets.UTF_8), contentByteArray)
            } else {
                return null
            }
        }

        private fun getEndOfHeaders(inputStream: InputStream): Long? {
            val blankLineSearcher = StreamSearcher("\r\n\r\n".toByteArray())

            val endOfBlankLineIndex = blankLineSearcher.search(inputStream)

            return if (endOfBlankLineIndex == -1L) null else endOfBlankLineIndex
        }
    }

    private fun getPartRanges(): MutableList<LongRange>? {
        val boundarySearcher = StreamSearcher("--${boundary}".toByteArray())
        val result = mutableListOf<LongRange>()
        var index = 0L

        while (true) {
            val lengthToEndOfBoundary = boundarySearcher.search(inputStream)

            if (lengthToEndOfBoundary == -1L) {
                // No boundary found (should find at least two).
                return null
            }

            val nextChar = inputStream.read()
            val followingChar = inputStream.read()

            if (index > 0L) {
                val endOfThisPartIndex = index + lengthToEndOfBoundary - (boundary.length + 2)
                result.add(index..endOfThisPartIndex)
            }

            if (nextChar == '-'.code && followingChar == '-'.code) {
                // This is the closing boundary.
                return result
            } else if (nextChar == '\r'.code && followingChar == '\n'.code) {
                // loop to look for next boundary.
            } else {
                // Unexpected character after boundary.
                return null
            }

            index += lengthToEndOfBoundary + 2
        }
    }
}