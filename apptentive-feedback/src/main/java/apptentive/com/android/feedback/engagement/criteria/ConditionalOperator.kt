package apptentive.com.android.feedback.engagement.criteria

interface ConditionalOperator {
    fun apply(first: Value, second: Value): Boolean
    fun description(description: String, second: Value, first: Value): String

    companion object {
        fun parse(value: String): ConditionalOperator = when (value) {
            "\$exists" -> exists
            "\$ne" -> ne
            "\$eq" -> eq
            "\$lt" -> lt
            "\$lte" -> lte
            "\$gt" -> gt
            "\$gte" -> gte
            "\$contains" -> contains
            "\$starts_with" -> starts_with
            "\$ends_with" -> ends_with
            "\$before" -> before
            "\$after" -> after
            else -> unknown
        }

        private val exists: ConditionalOperator by lazy {
            object : ConditionalOperator {
                override fun apply(first: Value, second: Value): Boolean {
                    TODO()
                }

                override fun description(description: String, second: Value, first: Value): String {
                    return "$description ('$first') exists"
                }
            }
        }

        private val ne: ConditionalOperator by lazy {
            object : ConditionalOperator {
                override fun apply(first: Value, second: Value): Boolean {
                    TODO()
                }

                override fun description(description: String, second: Value, first: Value): String {
                    return "$description ('$first') not equal to '$second'"
                }
            }
        }

        private val eq: ConditionalOperator by lazy {
            object : ConditionalOperator {
                override fun apply(first: Value, second: Value): Boolean {
                    TODO()
                }

                override fun description(description: String, second: Value, first: Value): String {
                    return "$description ('$first') equal to '$second'"
                }
            }
        }

        private val lt: ConditionalOperator by lazy {
            object : ConditionalOperator {
                override fun apply(first: Value, second: Value): Boolean {
                    TODO()
                }

                override fun description(description: String, second: Value, first: Value): String {
                    return "$description ($first) less than $second"
                }
            }
        }

        private val lte: ConditionalOperator by lazy {
            object : ConditionalOperator {
                override fun apply(first: Value, second: Value): Boolean {
                    TODO()
                }

                override fun description(description: String, second: Value, first: Value): String {
                    return "$description ($first) is less than or equal to $second"
                }
            }
        }

        private val gt: ConditionalOperator by lazy {
            object : ConditionalOperator {
                override fun apply(first: Value, second: Value): Boolean {
                    TODO()
                }

                override fun description(description: String, second: Value, first: Value): String {
                    return "$description ($first) greater than $second"
                }
            }
        }

        private val gte: ConditionalOperator by lazy {
            object : ConditionalOperator {
                override fun apply(first: Value, second: Value): Boolean {
                    TODO()
                }

                override fun description(description: String, second: Value, first: Value): String {
                    return "$description ($first) is greater than or equal to $second"
                }
            }
        }

        private val contains: ConditionalOperator by lazy {
            object : ConditionalOperator {
                override fun apply(first: Value, second: Value): Boolean {
                    TODO()
                }

                override fun description(description: String, second: Value, first: Value): String {
                    return "$description ('$first') contains '$second'"
                }
            }
        }

        private val starts_with: ConditionalOperator by lazy {
            object : ConditionalOperator {
                override fun apply(first: Value, second: Value): Boolean {
                    TODO()
                }

                override fun description(description: String, second: Value, first: Value): String {
                    return "$description ('$first') starts with '$second'"
                }
            }
        }

        private val ends_with: ConditionalOperator by lazy {
            object : ConditionalOperator {
                override fun apply(first: Value, second: Value): Boolean {
                    TODO()
                }

                override fun description(description: String, second: Value, first: Value): String {
                    return "$description ('$first') starts with '$second'"
                }
            }
        }

        private val before: ConditionalOperator by lazy {
            object : ConditionalOperator {
                override fun apply(first: Value, second: Value): Boolean {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun description(description: String, second: Value, first: Value): String {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }
            }
        }

        private val after: ConditionalOperator by lazy {
            object : ConditionalOperator {
                override fun apply(first: Value, second: Value): Boolean {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun description(description: String, second: Value, first: Value): String {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }
            }
        }

        private val unknown: ConditionalOperator by lazy {
            object : ConditionalOperator {
                override fun apply(first: Value, second: Value): Boolean {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun description(description: String, second: Value, first: Value): String {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }
            }
        }
    }
}