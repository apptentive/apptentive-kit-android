package apptentive.com.android.feedback.engagement.criteria

import kotlin.math.abs

typealias FieldPath = String

sealed class Value(val description: String) : Comparable<Value> {
    class Str(description: String, val value: String) : Value(description) {
        fun contains(other: Str) = value.contains(other.value, ignoreCase = true)
        fun startsWith(other: Str) = value.startsWith(other.value, ignoreCase = true)
        fun endsWith(other: Str) = value.endsWith(other.value, ignoreCase = true)

        override fun equals(other: Any?): Boolean {
            return other is Str && value.equals(other.value, ignoreCase = true)
        }

        override fun toString() = value
    }

    class Bool(description: String, val value: Boolean) : Value(description) {
        override fun equals(other: Any?): Boolean {
            return other is Bool && value == other.value
        }

        override fun toString() = value.toString()
    }

    class Number(description: String, val value: Double) : Value(description) {
        override fun equals(other: Any?): Boolean {
            return other is Number && abs(value - other.value) < 0.0000001
        }

        override fun toString() = value.toString()
    }

    class Null(description: String) : Value(description) {
        override fun toString() = "null"
    }

    override fun compareTo(other: Value): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    companion object {
        fun from(obj: Any?): Value = when (obj) {
            null -> Null("description")
            is String -> Str("description", obj)
            is Double -> Number("description", obj)
            is Boolean -> Bool("description", obj)
            else -> TODO()
        }
    }
}

val Value.isNull get() = this is Value.Null

interface TargetingState {
    fun getValue(fieldPath: FieldPath): Value
}

interface Clause {
    fun evaluate(state: TargetingState): Boolean
}