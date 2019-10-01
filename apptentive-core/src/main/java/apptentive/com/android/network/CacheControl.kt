package apptentive.com.android.network

data class CacheControl(
    val maxAgeSeconds: Int = -1
) {
    companion object {
        fun parse(value: String): CacheControl {
            for (token in tokenize(value)) {
                val (directive, param) = parseDirective(token)
                if ("max-age".equals(directive, ignoreCase = true)) {
                    val maxAgeSeconds = param?.toInt() ?: -1
                    return CacheControl(maxAgeSeconds = maxAgeSeconds)
                }
            }

            return CacheControl()
        }

        private fun tokenize(value: String): List<String> {
            val bracketStart = value.indexOf('[')
            val bracketEnd = value.lastIndexOf(']')
            val cleared = if (bracketStart == -1 && bracketEnd == -1) value else value.substring(
                bracketStart + 1,
                bracketEnd
            )
            return cleared.split(",").map { it.trim() }
        }

        private fun parseDirective(value: String): Pair<String, String?> {
            val index = value.indexOf('=')
            if (index == -1) {
                return Pair(value, null)
            }

            val directive = value.substring(0, index).trim()
            val param = value.substring(index + 1).trim()
            return Pair(directive, param)
        }
    }
}