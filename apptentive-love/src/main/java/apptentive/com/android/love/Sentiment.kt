package apptentive.com.android.love

enum class SentimentType {
    POSITIVE,
    NEGATIVE,
    NEUTRAL
}

class Sentiment(identifier: String, val type: SentimentType) : LoveEntity(identifier)


