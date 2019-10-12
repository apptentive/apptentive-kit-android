package apptentive.com.android.feedback.engagement.criteria

sealed class Value {
    data class String(private val value: kotlin.String) : Value(), Comparable<String> {
        fun contains(other: String) = value.contains(other.value, ignoreCase = true)

        fun startsWith(other: String) = value.startsWith(other.value, ignoreCase = true)

        fun endsWith(other: String) = value.endsWith(other.value, ignoreCase = true)

        override fun equals(other: Any?) =
            other is String && value.equals(other.value, ignoreCase = true)

        override fun compareTo(other: String) = value?.compareTo(other.value) ?: 0

        override fun toString() = value
    }

    data class Boolean(private val value: kotlin.Boolean) : Value() {
        override fun toString() = value.toString()
    }

    data class Number(private val value: Long) : Value(), Comparable<Number> {
        constructor(value: Int) : this(value.toLong())

        override fun compareTo(other: Number) = value.compareTo(other.value)

        override fun toString() = value.toString()
    }

    data class DateTime(private val seconds: Long) : Value(), Comparable<DateTime> {
        override fun compareTo(other: DateTime) = seconds.compareTo(other.seconds)

        override fun toString() = seconds.toString()
    }

    // FIXME: add support for semantic versioning https://semver.org/
    class Version(
        private val major: Long,
        private val minor: Long,
        private val patch: Long
    ) : Value(), Comparable<Version> {

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
    }

    object Null : Value() {
        override fun toString() = "null"
    }
}

operator fun <T : Value> Value.compareTo(other: T): Int =
    if (this.javaClass == other.javaClass && this is Comparable<*>) {
        this.compareTo(other)
    } else {
        0
    }

val Value.isNull get() = this is Value.Null