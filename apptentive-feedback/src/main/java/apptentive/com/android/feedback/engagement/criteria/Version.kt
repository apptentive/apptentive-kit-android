package apptentive.com.android.feedback.engagement.criteria

internal data class Version(
    private val major: Long,
    private val minor: Long,
    private val patch: Long,
    private val hotfix: Long,
    private val stringVersion: String? = null
) : Comparable<Version> {

    override fun compareTo(other: Version): Int {
        val majorCmp = major.compareTo(other.major)
        if (majorCmp != 0) {
            return majorCmp
        }

        val minorCmp = minor.compareTo(other.minor)
        if (minorCmp != 0) {
            return minorCmp
        }

        val patchCmp = patch.compareTo(other.patch)
        if (patchCmp != 0) {
            return patchCmp
        }

        val hotfixCmp = hotfix.compareTo(other.hotfix)
        if (hotfixCmp != 0) {
            return hotfixCmp
        }

        return when {
            stringVersion != null && other.stringVersion == null -> -1 // Expecting other to be a pre-release version
            stringVersion == null && other.stringVersion != null -> 1 // Expecting device app to be a pre-release version
            else -> 0 // Not comparing string versions (alpha vs beta), just assume equal.
        }
    }

    override fun toString() = "$major.$minor.$patch.$hotfix\' / \'$stringVersion"

    companion object {
        fun parse(value: String?): Version? {
            return if (value != null) tryParse(value) else null
        }

        private fun tryParse(value: String): Version {
            val components = value.split(".", "-")

            val numberRegex = Regex("^[0-9]+\$") // Only allows numbers and requires at least 1
            var hasExtra = false

            var major: Long = 0
            if (components.isNotEmpty()) {
                if (numberRegex.matches(components[0])) major = components[0].toLong()
                else hasExtra = true
            }

            var minor: Long = 0
            if (components.size > 1) {
                if (numberRegex.matches(components[1])) minor = components[1].toLong()
                else hasExtra = true
            }

            var patch: Long = 0
            if (components.size > 2) {
                if (numberRegex.matches(components[2])) patch = components[2].toLong()
                else hasExtra = true
            }

            var hotfix: Long = 0
            if (components.size > 3) {
                if (numberRegex.matches(components[3])) hotfix = components[3].toLong()
                else hasExtra = true
            }

            return Version(
                major = major,
                minor = minor,
                patch = patch,
                hotfix = hotfix,
                stringVersion = if (hasExtra) value else null
            )
        }
    }
}
