package apptentive.com.android.util

import java.util.*

fun createStringTable(rows: List<Array<Any?>>): String {
    val columnSizes = IntArray(rows[0].size)
    for (row in rows) {
        for (i in row.indices) {
            columnSizes[i] = Math.max(
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
    for (row in rows) {
        result.append("\n")
        for (i in row.indices) {
            if (i > 0) {
                result.append(" | ")
            }
            result.append(String.format("%-${columnSizes[i]}s", row[i]))
        }
    }
    result.append("\n").append(line)
    return result.toString()
}

fun parseInt(value: String?) = try {
    if (value != null) Integer.valueOf(value) else null
} catch (e: Exception) {
    null
}

fun tryFormat(format: String, vararg args: Any?) = try {
    String.format(Locale.US, format, *args)
} catch (e: java.lang.Exception) {
    format
}