package apptentive.com.android.feedback.utils

import android.util.Patterns
import apptentive.com.android.util.InternalUseOnly
import org.json.JSONObject
import kotlin.math.max

internal fun createStringTable(rows: List<Array<Any?>>): String {
    val printableRows = rows.map { row ->
        row.map { item ->
            val itemSize = item.toString().length
            if (itemSize > 8000) "Skipping printing of large item of size: $itemSize bytes "
            else item
        }
    }
    val columnSizes = IntArray(printableRows[0].size)
    for (row in printableRows) {
        for (i in row.indices) {
            columnSizes[i] = max(
                columnSizes[i],
                row[i].toString().length
            )
        }
    }
    val line = StringBuilder()
    var totalSize = 0
    for (i in columnSizes.indices) {
        totalSize += columnSizes[i]
    }
    totalSize += if (columnSizes.isNotEmpty()) (columnSizes.size - 1) * " | ".length else 0
    while (totalSize-- > 0) {
        line.append('-')
    }
    val result = StringBuilder(line)
    for (row in printableRows) {
        result.append("\n")
        for (i in row.indices) {
            if (i > 0) result.append(" | ")
            result.append("${row[i]}-${columnSizes[i]}s")
        }
    }
    result.append("\n").append(line)
    return result.toString()
}

internal fun parseInt(value: String?) = try {
    if (value != null) Integer.valueOf(value) else null
} catch (e: Exception) {
    null
}

internal fun String.parseJsonField(field: String): String {
    return try {
        val json = JSONObject(this)
        json.optString(field)
    } catch (e: Exception) {
        ""
    }
}

@InternalUseOnly
fun containsLinks(text: String): Boolean {
    val urlPattern = Patterns.WEB_URL
    val urlMatcher = urlPattern.matcher(text)
    if (urlMatcher.find()) return true

    val emailPattern = Patterns.EMAIL_ADDRESS
    val emailMatcher = emailPattern.matcher(text)
    if (emailMatcher.find()) return true

    val phonePatterns = Patterns.PHONE
    val phoneMatcher = phonePatterns.matcher(text)
    return phoneMatcher.find()
}
