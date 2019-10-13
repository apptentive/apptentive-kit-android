package apptentive.com.android.feedback.engagement.criteria

interface ConditionalOperator {
    fun apply(first: Any?, second: Any?): Boolean
    fun description(description: String, first: Any?, second: Any?): String

    companion object {
        internal const val EXISTS = "\$exists"
        internal const val NE = "\$ne"
        internal const val EQ = "\$eq"
        internal const val LT = "\$lt"
        internal const val LTE = "\$lte"
        internal const val GT = "\$gt"
        internal const val GTE = "\$gte"
        internal const val CONTAINS = "\$contains"
        internal const val STARTS_WITH = "\$starts_with"
        internal const val ENDS_WITH = "\$ends_with"
        internal const val BEFORE = "\$before"
        internal const val AFTER = "\$after"

        fun parse(value: String): ConditionalOperator {
            return when (value) {
                EXISTS -> exists
                NE -> ne
                EQ -> eq
                LT -> lt
                LTE -> lte
                GT -> gt
                GTE -> gte
                CONTAINS -> contains
                STARTS_WITH -> starts_with
                ENDS_WITH -> ends_with
                BEFORE -> before
                AFTER -> after
                else -> unknown
            }
        }

        private val exists: ConditionalOperator by lazy {
            object : ConditionalOperator {
                override fun apply(first: Any?, second: Any?): Boolean {
                    if (second == null) {
                        return false
                    }
                    if (second !is Boolean) {
                        return false
                    }
                    val exists = first != null
                    return exists == second
                }

                override fun description(description: String, first: Any?, second: Any?): String {
                    return "$description ('$first') exists"
                }
            }
        }

        private val ne: ConditionalOperator by lazy {
            object : ConditionalOperator {
                override fun apply(first: Any?, second: Any?): Boolean {
                    if (first == null || second == null) {
                        return false
                    }
                    if (first.javaClass != second.javaClass) {
                        return false
                    }
                    if (first is String && second is String) {
                        return !first.equals(second, ignoreCase = true)
                    }
                    return compare(first, second) != 0
                }

                override fun description(description: String, first: Any?, second: Any?): String {
                    return "$description ('$first') not equal to '$second'"
                }
            }
        }

        private val eq: ConditionalOperator by lazy {
            object : ConditionalOperator {
                override fun apply(first: Any?, second: Any?): Boolean {
                    if (first == null && second == null) {
                        return true
                    }
                    if (first == null || second == null) {
                        return false
                    }
                    if (first.javaClass != second.javaClass) {
                        return false
                    }
                    if (first is String && second is String) {
                        return first.equals(second, ignoreCase = true)
                    }
                    return compare(first, second) == 0
                }

                override fun description(description: String, first: Any?, second: Any?): String {
                    return "$description ('$first') equal to '$second'"
                }
            }
        }

        private val lt: ConditionalOperator by lazy {
            object : ConditionalOperator {
                override fun apply(first: Any?, second: Any?): Boolean {
                    if (first == null || second == null) {
                        return false
                    }
                    if (first.javaClass != second.javaClass) {
                        return false
                    }
                    return compare(first, second) < 0
                }

                override fun description(description: String, first: Any?, second: Any?): String {
                    return "$description ($first) less than $second"
                }
            }
        }

        private val lte: ConditionalOperator by lazy {
            object : ConditionalOperator {
                override fun apply(first: Any?, second: Any?): Boolean {
                    if (first == null || second == null) {
                        return false
                    }
                    if (first.javaClass != second.javaClass) {
                        return false
                    }

                    return compare(first, second) <= 0
                }

                override fun description(description: String, first: Any?, second: Any?): String {
                    return "$description ($first) is less than or equal to $second"
                }
            }
        }

        private val gt: ConditionalOperator by lazy {
            object : ConditionalOperator {
                override fun apply(first: Any?, second: Any?): Boolean {
                    if (first == null || second == null) {
                        return false
                    }
                    if (first.javaClass != second.javaClass) {
                        return false
                    }
                    return compare(first, second) > 0
                }

                override fun description(description: String, first: Any?, second: Any?): String {
                    return "$description ($first) greater than $second"
                }
            }
        }

        private val gte: ConditionalOperator by lazy {
            object : ConditionalOperator {
                override fun apply(first: Any?, second: Any?): Boolean {
                    if (first == null || second == null) {
                        return false
                    }
                    if (first.javaClass != second.javaClass) {
                        return false
                    }
                    return compare(first, second) >= 0
                }

                override fun description(description: String, first: Any?, second: Any?): String {
                    return "$description ($first) is greater than or equal to $second"
                }
            }
        }

        private val contains: ConditionalOperator by lazy {
            object : ConditionalOperator {
                override fun apply(first: Any?, second: Any?): Boolean {
                    if (first == null || second == null) {
                        return false
                    }
                    if (first !is String || second !is String) {
                        return false
                    }
                    return first.contains(second, ignoreCase = true)
                }

                override fun description(description: String, first: Any?, second: Any?): String {
                    return "$description ('$first') contains '$second'"
                }
            }
        }

        private val starts_with: ConditionalOperator by lazy {
            object : ConditionalOperator {
                override fun apply(first: Any?, second: Any?): Boolean {
                    if (first !is String || second !is String) {
                        return false
                    }
                    return first.startsWith(second, ignoreCase = true)
                }

                override fun description(description: String, first: Any?, second: Any?): String {
                    return "$description ('$first') starts with '$second'"
                }
            }
        }

        private val ends_with: ConditionalOperator by lazy {
            object : ConditionalOperator {
                override fun apply(first: Any?, second: Any?): Boolean {
                    if (first !is String || second !is String) {
                        return false
                    }
                    return first.endsWith(second, ignoreCase = true)
                }

                override fun description(description: String, first: Any?, second: Any?): String {
                    return "$description ('$first') starts with '$second'"
                }
            }
        }

        private val before: ConditionalOperator by lazy {
            object : ConditionalOperator {
                override fun apply(first: Any?, second: Any?): Boolean {
                    if (first !is DateTime) {
                        return false
                    }
                    if (second !is DateTime) {
                        return false
                    }
                    return first < second
                }

                override fun description(description: String, first: Any?, second: Any?): String {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }
            }
        }

        private val after: ConditionalOperator by lazy {
            object : ConditionalOperator {
                override fun apply(first: Any?, second: Any?): Boolean {
                    if (first !is DateTime) {
                        return false
                    }
                    if (second !is DateTime) {
                        return false
                    }
                    return first > second
                }

                override fun description(description: String, first: Any?, second: Any?): String {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }
            }
        }

        private val unknown: ConditionalOperator by lazy {
            object : ConditionalOperator {
                override fun apply(first: Any?, second: Any?): Boolean {
                    return false
                }

                override fun description(description: String, first: Any?, second: Any?): String {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
private fun compare(a: Any, b: Any): Int {
    if (a.javaClass != b.javaClass) {
        return 0
    }
    val c1 = a as? Comparable<Any>
    val c2 = b as? Comparable<Any>
    if (c1 != null && c2 != null) {
        return a.compareTo(b)
    }

    return 0
}