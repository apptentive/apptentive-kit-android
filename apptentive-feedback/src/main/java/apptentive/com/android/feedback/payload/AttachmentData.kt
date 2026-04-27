package apptentive.com.android.feedback.payload

data class AttachmentData(val data: ByteArray = ByteArray(0), val dataFilePath: String = "") {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AttachmentData

        return when {
            !data.contentEquals(other.data) -> false
            dataFilePath != other.dataFilePath -> false
            else -> true
        }
    }

    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + dataFilePath.hashCode()
        return result
    }
}
