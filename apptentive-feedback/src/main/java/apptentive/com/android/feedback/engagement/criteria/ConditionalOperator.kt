package apptentive.com.android.feedback.engagement.criteria

import apptentive.com.android.feedback.engagement.interactions.InteractionResponse

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

        internal fun parse(value: String): ConditionalOperator {
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
                    if (first is Set<*>) {
                        return first.none {
                            when {
                                it is InteractionResponse.IdResponse && second is String -> it.id.equals(second, ignoreCase = true)
                                it is InteractionResponse.LongResponse && second is Long -> compare(it.response, second) == 0
                                it is InteractionResponse.StringResponse && second is String -> it.response.equals(second, ignoreCase = true)
                                it is InteractionResponse.OtherResponse && second is String -> {
                                    it.id.equals(second, ignoreCase = true) || it.response.equals(second, ignoreCase = true)
                                }
                                else -> false
                            }
                        }
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
                    if (first is Set<*>) {
                        return first.any {
                            when {
                                it is InteractionResponse.IdResponse && second is String -> it.id.equals(second, ignoreCase = true)
                                it is InteractionResponse.LongResponse && second is Long -> compare(it.response, second) == 0
                                it is InteractionResponse.StringResponse && second is String -> it.response.equals(second, ignoreCase = true)
                                it is InteractionResponse.OtherResponse && second is String -> {
                                    it.id.equals(second, ignoreCase = true) || it.response.equals(second, ignoreCase = true)
                                }
                                else -> false
                            }
                        }
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
                    if (first is Set<*> && second is Long) {
                        return first.any {
                            if (it is InteractionResponse.LongResponse) compare(it.response, second) < 0
                            else false
                        }
                    }
                    if (first.javaClass != second.javaClass) {
                        return false
                    }
                    return compare(first, second) < 0
                }

                override fun description(description: String, first: Any?, second: Any?): String {
                    return "$description ('$first') less than '$second'"
                }
            }
        }

        private val lte: ConditionalOperator by lazy {
            object : ConditionalOperator {
                override fun apply(first: Any?, second: Any?): Boolean {
                    if (first == null || second == null) {
                        return false
                    }
                    if (first is Set<*> && second is Long) {
                        return first.any {
                            if (it is InteractionResponse.LongResponse) compare(it.response, second) <= 0
                            else false
                        }
                    }
                    if (first.javaClass != second.javaClass) {
                        return false
                    }
                    return compare(first, second) <= 0
                }

                override fun description(description: String, first: Any?, second: Any?): String {
                    return "$description ('$first') is less than or equal to '$second'"
                }
            }
        }

        private val gt: ConditionalOperator by lazy {
            object : ConditionalOperator {
                override fun apply(first: Any?, second: Any?): Boolean {
                    if (first == null || second == null) {
                        return false
                    }
                    if (first is Set<*> && second is Long) {
                        return first.any {
                            if (it is InteractionResponse.LongResponse) compare(it.response, second) > 0
                            else false
                        }
                    }
                    if (first.javaClass != second.javaClass) {
                        return false
                    }
                    return compare(first, second) > 0
                }

                override fun description(description: String, first: Any?, second: Any?): String {
                    return "$description ('$first') greater than '$second'"
                }
            }
        }

        private val gte: ConditionalOperator by lazy {
            object : ConditionalOperator {
                override fun apply(first: Any?, second: Any?): Boolean {
                    if (first == null || second == null) {
                        return false
                    }
                    if (first is Set<*> && second is Long) {
                        return first.any {
                            if (it is InteractionResponse.LongResponse) compare(it.response, second) >= 0
                            else false
                        }
                    }
                    if (first.javaClass != second.javaClass) {
                        return false
                    }
                    return compare(first, second) >= 0
                }

                override fun description(description: String, first: Any?, second: Any?): String {
                    return "$description ('$first') is greater than or equal to '$second'"
                }
            }
        }

        private val contains: ConditionalOperator by lazy {
            object : ConditionalOperator {
                override fun apply(first: Any?, second: Any?): Boolean {
                    if (first == null || second == null) {
                        return false
                    }
                    if (first is Set<*> && second is String) {
                        return first.any {
                            when (it) {
                                is InteractionResponse.StringResponse -> it.response.contains(second, ignoreCase = true)
                                is InteractionResponse.OtherResponse -> it.response?.contains(second, ignoreCase = true) == true
                                else -> false
                            }
                        }
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
                    if (first is Set<*> && second is String) {
                        return first.any {
                            when (it) {
                                is InteractionResponse.StringResponse -> it.response.startsWith(second, ignoreCase = true)
                                is InteractionResponse.OtherResponse -> it.response?.startsWith(second, ignoreCase = true) == true
                                else -> false
                            }
                        }
                    }
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
                    if (first is Set<*> && second is String) {
                        return first.any {
                            when (it) {
                                is InteractionResponse.StringResponse -> it.response.endsWith(second, ignoreCase = true)
                                is InteractionResponse.OtherResponse -> it.response?.endsWith(second, ignoreCase = true) == true
                                else -> false
                            }
                        }
                    }
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
                    return "$description ('${toPrettyDate(first)}') before date '${toPrettyDate(second)}'"
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
                    return "$description ('${toPrettyDate(first)}') after date '${toPrettyDate(second)}'"
                }
            }
        }

        private val unknown: ConditionalOperator by lazy {
            object : ConditionalOperator {
                override fun apply(first: Any?, second: Any?): Boolean {
                    return false
                }

                override fun description(description: String, first: Any?, second: Any?): String {
                    return "Unknown field '$description'"
                }
            }
        }

        private fun toPrettyDate(value: Any?): String {
            return value.toString()
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
