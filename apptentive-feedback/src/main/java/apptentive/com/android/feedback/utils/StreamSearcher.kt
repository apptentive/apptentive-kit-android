package apptentive.com.android.feedback.utils

import java.io.IOException
import java.io.InputStream
import java.util.Arrays

internal class StreamSearcher(private var pattern: ByteArray) {
    // Adapted from: https://github.com/twitter/elephant-bird/blob/master/core/src/main/java/com/twitter/elephantbird/util/StreamSearcher.java

    private var borders: IntArray = IntArray(pattern.size + 1)

    companion object {
        // An upper bound on pattern length for searching. Results are undefined for longer patterns.
        const val MAX_PATTERN_LENGTH = 1024
    }

    init {
        setPattern(pattern)
    }

    /**
     * Sets a new pattern for this StreamSearcher to use.
     *
     * @param pattern the pattern the StreamSearcher will look for in future calls to search(...)
     */
    private fun setPattern(pattern: ByteArray) {
        this.pattern = Arrays.copyOf(pattern, pattern.size)
        borders = IntArray(pattern.size + 1)
        preProcess()
    }

    /**
     * Searches for the next occurrence of the pattern in the stream, starting from the current stream position. Note
     * that the position of the stream is changed. If a match is found, the stream points to the end of the match -- i.e. the
     * byte AFTER the pattern. Else, the stream is entirely consumed. The latter is because InputStream semantics make it difficult to have
     * another reasonable default, i.e. leave the stream unchanged.
     *
     * @return bytes consumed if found, -1 otherwise.
     */
    @Throws(IOException::class)
    fun search(stream: InputStream): Long {
        var bytesRead: Long = 0
        var b: Int
        var j = 0
        while (stream.read().also { b = it } != -1) {
            bytesRead++
            while (j >= 0 && b.toByte() != pattern[j]) {
                j = borders[j]
            }
            // Move to the next character in the pattern.
            j++
            // If we've matched up to the full pattern length, we found it.  Return,
            // which will automatically save our position in the InputStream at the point immediately
            // following the pattern match.
            if (j == pattern.size) {
                return bytesRead
            }
        }
        // No match, Note that the stream is now completely consumed.
        return -1
    }

    /**
     * Builds up a table of longest "borders" for each prefix of the pattern to find. This table is stored internally
     * and aids in the implementation of the Knuth-Moore-Pratt string search.
     * For more information, see: http://www.inf.fh-flensburg.de/lang/algorithmen/pattern/kmpen.htm.
     */
    private fun preProcess() {
        var i = 0
        var j = -1
        borders[i] = j
        while (i < pattern.size) {
            while (j >= 0 && pattern[i] != pattern[j]) {
                j = borders[j]
            }
            borders[++i] = ++j
        }
    }
}
