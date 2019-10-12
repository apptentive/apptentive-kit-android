package apptentive.com.android.feedback.engagement.criteria

data class FieldValue(val value: Value, val description: String) {
    companion object {
        fun boolean(value: Boolean?, description: String): FieldValue {
            val bool = if (value != null) Value.Boolean(value) else Value.Null
            return FieldValue(bool, description)
        }

        fun number(value: Int?, description: String): FieldValue {
            val number = if (value != null) Value.Number(value) else Value.Null
            return FieldValue(number, description)
        }

        fun string(value: String?, description: String): FieldValue {
            val string = if (value != null) Value.String(value) else Value.Null
            return FieldValue(string, description)
        }

        fun dateTime(seconds: Long?, description: String): FieldValue {
            val dateTime = if (seconds != null) Value.DateTime(seconds) else Value.Null
            return FieldValue(dateTime, description)
        }

        // FIXME: implement version
        fun version(value: String?, description: String): FieldValue {
            TODO()
        }
    }
}