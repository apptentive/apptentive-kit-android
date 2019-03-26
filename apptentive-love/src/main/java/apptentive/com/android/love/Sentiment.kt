package apptentive.com.android.love

enum class SentimentType {
    POSITIVE,
    NEGATIVE,
    NEUTRAL
}

class Sentiment(identifier: String, val type: SentimentType) : LoveEntity(identifier) {
    override fun toString(): String {
        return "${super.toString()}: $type"
    }
}


