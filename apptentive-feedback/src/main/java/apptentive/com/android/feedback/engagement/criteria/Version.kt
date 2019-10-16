package apptentive.com.android.feedback.engagement.criteria

// FIXME: add support for semantic versioning https://semver.org/
// FIXME: unit testing
data class Version(
    private val major: Long,
    private val minor: Long,
    private val patch: Long
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

        return patch.compareTo(other.patch)
    }

    override fun toString() = "$major.$minor.$patch"

    companion object {
        fun tryParse(value: kotlin.String?): Version? {
            return if (value != null) parse(value) else null
        }

        fun parse(value: kotlin.String): Version {
            val components = value.split(".")
            return Version(
                major = components[0].toLong(),
                minor = if (components.size > 1) components[1].toLong() else 0,
                patch = if (components.size > 2) components[2].toLong() else 0
            )
        }
    }
}